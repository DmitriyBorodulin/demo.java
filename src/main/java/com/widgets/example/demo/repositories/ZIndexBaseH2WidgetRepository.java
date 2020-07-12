package com.widgets.example.demo.repositories;

import com.widgets.example.demo.exceptions.InvalidFilterParamsException;
import com.widgets.example.demo.models.FilterParams;
import com.widgets.example.demo.models.ReadonlyWidget;
import com.widgets.example.demo.models.Widget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

interface ExecuteStatementInner<T>
{
   T doOperation(Statement statement) throws SQLException;
}

interface ExecutePreparedStatementInner<T>
{
    T doOperation(PreparedStatement statement) throws SQLException;
}

interface ExecBlockingOperationInner
{
    void doOperation(Connection connection) throws SQLException;
}

@Component
public class ZIndexBaseH2WidgetRepository extends ZIndexBaseWidgetRepository implements IZIndexBasedWidgetRepository {

    public final Environment environment;
    private final Connection connection;
    private final String databaseName;
    private final String CreateWidgetsTableDDL = "Create table Widgets (x INT , y INT , width INT , height INT , id UUID NOT NULL DEFAULT RANDOM_UUID() PRIMARY KEY,  zLevel int, changeDate timestamp)";


    public ZIndexBaseH2WidgetRepository(@Autowired Environment environment) throws SQLException {
        this.environment = environment;
        databaseName = environment.getProperty("spring.datasource.url")+UUID.randomUUID().toString();
        connection =  startConnection();
        executeStatement(statement -> {
            statement.executeUpdate(CreateWidgetsTableDDL);
            return null;
        });
    }

    private final ReentrantLock updateLock = new ReentrantLock();

    private void execConnectionBlockingOperation(ExecBlockingOperationInner operation) throws SQLException   {
        Connection externalConnection = null;
        try
        {
            updateLock.lock();
            externalConnection = startConnection();
            operation.doOperation(externalConnection);
            externalConnection.commit();
        }
        catch (SQLException exception)
        {
            if (externalConnection != null)
                externalConnection.rollback();
        }
        finally
        {
            updateLock.unlock();
        }
    }

    private Connection startConnection() throws SQLException {
        var connection = DriverManager.getConnection(databaseName,
                environment.getProperty("spring.datasource.username"),
                environment.getProperty("spring.datasource.password"));
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        connection.setAutoCommit(false);
        return connection;
    }

    private ReadonlyWidget rowToWidget(ResultSet rs) throws SQLException {
        var widget = new Widget(rs.getLong(1),rs.getLong(2),rs.getLong(3),rs.getLong(4),rs.getLong(5));
        Calendar date = Calendar.getInstance();
        date.setTime(rs.getTimestamp(6,date));
        return new ReadonlyWidget(widget,date,UUID.fromString(rs.getString(7)));
    }

    private <T> T executeStatement(ExecuteStatementInner<T> operation) throws SQLException {
        return executeStatement(operation,null);
    }

    private <T> T executeStatement(ExecuteStatementInner<T> operation, Connection externalConnection) throws SQLException {
        T res = null;
        Statement stmt = null;
        try {
            if (externalConnection != null)
                stmt = externalConnection.createStatement();
            else
                stmt = connection.createStatement();
            res = operation.doOperation(stmt);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
        return res;
    }

    private void moveZIndexTo1(long zIndex,UUID exceptId,Connection externalConnection) throws SQLException {
        executePreparedStatement("Update Widgets set zLevel = -zLevel-1 where zLevel = ? and id <> ?",(statement ->
        {
            statement.setString(2, exceptId.toString());
            var innerZLevel = zIndex;
            statement.setLong(1,innerZLevel);
            while (statement.executeUpdate() > 0)
                statement.setLong(1,++innerZLevel);
            return null;
        }),externalConnection);
        executeStatement(statement -> {
            statement.executeUpdate("Update Widgets set zLevel = -zLevel where zLevel < 0");
            return null;
        },externalConnection);
    }

    private <T> T executePreparedStatement(String sql, ExecutePreparedStatementInner<T> operation) throws SQLException {
        return executePreparedStatement(sql,operation,null);
    }

    private <T> T executePreparedStatement(String sql, ExecutePreparedStatementInner<T> operation, Connection externalConnection) throws SQLException {
        T res;
        PreparedStatement stmt = null;
        try {
            if (externalConnection != null)
                stmt = externalConnection.prepareStatement(sql , Statement.RETURN_GENERATED_KEYS);
            else
                stmt = connection.prepareStatement(sql , Statement.RETURN_GENERATED_KEYS);
            res = operation.doOperation(stmt);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
        return res;
    }

    private void setChangeWidgetQueryParams(Widget widget, PreparedStatement statement) throws SQLException {
        statement.setLong(1,widget.x);
        statement.setLong(2,widget.y);
        statement.setLong(3,widget.width);
        statement.setLong(4,widget.height);
        statement.setLong(5,widget.zLevel);
        Timestamp date = Timestamp.from(Calendar.getInstance().toInstant());
        statement.setTimestamp(6, date);
    }

    @Override
    public ReadonlyWidget removeWidget(UUID widgetId) throws SQLException {
        var result = getWidget(widgetId);
        if (result != null)
        {
            execConnectionBlockingOperation((externalConnection) -> executePreparedStatement("delete Widgets where id = ?",(statement -> {
                statement.setString(1,widgetId.toString());
                statement.executeUpdate();
                return null;
            }),externalConnection));
        }
        return result;
    }

    @Override
    public ReadonlyWidget addWidget(Widget widget) throws SQLException {
        var context = new Object() {
            UUID id = null;
        };
        execConnectionBlockingOperation((externalConnection) -> {
            context.id = executePreparedStatement("insert into Widgets (x,y,width,height,zLevel,changeDate) values (?,?,?,?,?,?)",
                    (statement -> {
                        setChangeWidgetQueryParams(widget, statement);
                        statement.executeUpdate();
                        ResultSet rs = statement.getGeneratedKeys();
                        if (rs.next())
                            return rs.getObject(1, UUID.class);
                        return null;
                    }),externalConnection);
            moveZIndexTo1(widget.zLevel,context.id,externalConnection);

        });
        return getWidget(context.id);
    }

    @Override
    public ReadonlyWidget updateWidget(Widget widget) throws SQLException {
        UUID id = widget.getId();
        execConnectionBlockingOperation((externalConnection) -> {
            executePreparedStatement("update Widgets set x = ?,y = ?,width = ?,height = ?,zLevel = ?,changeDate = ? where id = ?",(statement -> {
                setChangeWidgetQueryParams(widget, statement);
                statement.setString(7,id.toString());
                return statement.executeUpdate();
            }),externalConnection);
            moveZIndexTo1(widget.zLevel,widget.getId(),externalConnection);
        });
        return getWidget(id);
    }

    @Override
    public List<ReadonlyWidget> getWidgets(FilterParams filterParams) throws InvalidFilterParamsException, SQLException {
        super.getWidgets(filterParams);
        var filterSql = filterParams != null ? "where x >= ? and y >= ? and x+width >= ? and y+height >= ?" : "";
        return executePreparedStatement("Select x,y,width,height,zLevel,changeDate, id from Widgets "+filterSql+" order by zLevel",(statement -> {
            if (filterParams != null)
            {
                statement.setLong(1,filterParams.x);
                statement.setLong(2,filterParams.y);
                statement.setLong(3,filterParams.x+filterParams.width);
                statement.setLong(4,filterParams.y+filterParams.height);
            }
            ResultSet rs = statement.executeQuery();
            var result = new ArrayList<ReadonlyWidget>();
            while ( rs.next() )
                result.add(rowToWidget(rs));
            return result;
        }));
    }

    @Override
    public ReadonlyWidget getWidget(UUID id) throws SQLException {
        return executePreparedStatement("Select x,y,width,height,zLevel,changeDate, id from Widgets where id = ?",(statement -> {
            statement.setString(1,id.toString());
            ResultSet rs = statement.executeQuery();
            if ( rs.next() )
            {
                return rowToWidget(rs);
            }
            return null;
        }));
    }
}
