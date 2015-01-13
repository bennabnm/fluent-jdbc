package org.fluentjdbc.internal.query;

import org.fluentjdbc.api.FluentJdbc;
import org.fluentjdbc.api.FluentJdbcBuilder;
import org.fluentjdbc.api.FluentJdbcException;
import org.fluentjdbc.api.query.Mapper;
import org.fluentjdbc.internal.query.UpdateResultInternal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FluentJdbcSelectTest {
    static final String query = "SELECT * FROM BAR";
    static final String column = "FOO";


    static String param1 = "lille";
    static String param2 = "lamb";
    static String result1 = "1";
    static String result2 = "2";
    static String result3 = "3";

    @Mock
    Connection connection;
    @Mock
    PreparedStatement preparedStatement;
    @Mock
    ResultSet resultset;
    FluentJdbc fluentJdbc;

    @Before
    public void setUp() throws SQLException {
        when(connection.prepareStatement(query)).thenReturn(preparedStatement);
        fluentJdbc = new FluentJdbcBuilder().connectionProvider((q) -> {
            q.receive(connection);
        }).build();
    }

    @Test
    public void selectList() throws SQLException {
        mockSelectData();
        List<Dummy> dummies = fluentJdbc.query().select(query).params(param1, param2).listResult(dummyMapper);
        assertResult(dummies);
        verifyQuerying();
    }

    @Test
    public void selectListWithFiltering() throws SQLException {
        mockSelectData();
        List<Dummy> dummies = fluentJdbc.query()
                .select(query)
                .params(param1, param2)
                .filter((Dummy dummy) -> dummy.foo.equals(result1))
                .listResult(dummyMapper);
        assertThat(dummies.size(), is(equalTo(1)));
        assertThat(dummies.get(0).foo, is(equalTo(result1)));
        verifyQuerying();
    }

    @Test
    public void selectSet() throws SQLException {
        mockSelectData();
        Set<Dummy> dummies = fluentJdbc.query().select(query).params(param1, param2).setResult(dummyMapper);
        assertThat(dummies.size(), is(3));
        verifyQuerying();
    }

    @Test
    public void selectSingle() throws SQLException {
        mockSelectData();
        Dummy d = fluentJdbc.query().select(query).params(param1, param2).singleResult(dummyMapper);
        assertThat(d.foo, is(equalTo(result1)));
        verifyQuerying();
    }

    @Test(expected = FluentJdbcException.class)
    public void selectSingleWithoutResults() throws SQLException {
        mockEmptySelectData();
        fluentJdbc.query().select(query).params(param1, param2).singleResult(dummyMapper);
    }

    @Test
    public void selectFirst() throws SQLException {
        mockSelectData();
        Optional<Dummy> dummy = fluentJdbc.query().select(query).params(param1, param2).firstResult(dummyMapper);
        assertThat(dummy.isPresent(), is(true));
        assertThat(dummy.get().foo, is(equalTo(result1)));
        verifyQuerying();
    }

    @Test
    public void selectFirstWithoutResults() throws SQLException {
        mockEmptySelectData();
        Optional<Dummy> dummy = fluentJdbc.query().select(query).params(param1, param2).firstResult(dummyMapper);
        assertThat(dummy.isPresent(), is(false));
        verifyQuerying();
    }

    private void assertResult(List<Dummy> dummyList) throws SQLException {
        assertThat(dummyList.size(), is(equalTo(3)));
        assertThat(dummyList.get(0).foo, is(equalTo(result1)));
        assertThat(dummyList.get(1).foo, is(equalTo(result2)));
        assertThat(dummyList.get(2).foo, is(equalTo(result3)));
        verify(preparedStatement).executeQuery();
        verify(resultset, times(4)).next();
    }

    private void mockSelectData() {
        try {
            when(preparedStatement.executeQuery()).thenReturn(resultset);
            when(resultset.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
            when(resultset.getString(column)).thenReturn(result1).thenReturn(result2).thenReturn(result3);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void mockEmptySelectData() {
        try {
            when(preparedStatement.executeQuery()).thenReturn(resultset);
            when(resultset.next()).thenReturn(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private void verifyQuerying() throws SQLException {
        verify(connection).prepareStatement(query);
        verify(preparedStatement).setObject(1, param1);
        verify(preparedStatement).setObject(2, param2);
        verify(preparedStatement).executeQuery();
        verify(preparedStatement).close();
    }

    static Mapper<Dummy> dummyMapper = rs -> new Dummy(rs.getString(column));

    static class Dummy {
        final String foo;

        Dummy(String foo) {
            this.foo = foo;
        }
    }
}
