package com.javacodegeeks.testng.spring;

import static org.springframework.test.context.transaction.TestTransaction.end;
import static org.springframework.test.context.transaction.TestTransaction.flagForCommit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration("tran_context.xml")
public class SpringTestNGTransactionExample extends	AbstractTransactionalTestNGSpringContextTests {
	private String method;

	@BeforeMethod
	public void saveMethodName(Method method) {
		this.method = method.getName();
	}

	@Test
	public void tran() {
		System.out.println("tran: verify JdbcTemplate is not null");
		assertNull(jdbcTemplate);
	}

	@BeforeTransaction
	public void beforeTransaction() {
		System.out.println("before transaction starts, delete all employees and re-run employee script");
		deleteFromTables("employee");
		executeSqlScript("classpath:/com/javacodegeeks/testng/spring/data.sql",	false);
	}

	@Test
	public void insertEmployeeAndCommit() {
		System.out.println("insertEmployeeAndCommit: insert employee 'Bill' and commit");
		String emp = "Bill";
		jdbcTemplate.update("insert into employee(name) values (?)", emp);
		assertEquals(countRowsInTableWhere("employee", "name='" + emp + "'"), 1);
		flagForCommit();
		end();
	}
	
	@Test
	public void insertEmployeeWithRollbackAsDefault() {
		System.out.println("insertEmployeeWithRollbackAsDefault: insert employee 'Bill', rollback by default");
		String emp = "Bill";
		jdbcTemplate.update("insert into employee(name) values (?)", emp);
		assertEquals(countRowsInTableWhere("employee", "name='" + emp + "'"), 1);
	}
	
	@Test
	@Rollback(false)
	public void insertEmployeeWithCommitAsDefault() {
		System.out.println("insertEmployeeWithCommitAsDefault: insert employee 'Bill', commit by default");
		String emp = "Bill";
		jdbcTemplate.update("insert into employee(name) values (?)", emp);
		assertEquals(countRowsInTableWhere("employee", "name='" + emp + "'"), 1);
	}
	
	@Test
	@Sql({"additional_data.sql"})
	public void insertEmployeeUsingSqlAnnotation() {
		System.out.println("insertEmployeeUsingSqlAnnotation: run additional sql using @sql annotation, rollback by default");
		assertEquals(countRowsInTableWhere("employee", "name='John'"), 1);
	}

	@AfterTransaction
	public void afterTransaction() {
		switch (method) {
		case "insertEmployeeAndCommit":
			assertEmployees("Bill", "Joe", "Sam");
			System.out.println("insertEmployeeAndCommit: employees found: 'Bill', 'Joe' and 'Sam'");
			break;
		case "insertEmployeeWithRollbackAsDefault":
			System.out.println("insertEmployeeWithRollbackAsDefault: employees found: 'Joe' and 'Sam'");
			assertEmployees("Joe", "Sam");
			break;
		case "insertEmployeeWithCommitAsDefault":
			System.out.println("insertEmployeeWithCommitAsDefault: employees found: 'Bill', 'Joe' and 'Sam'");
			assertEmployees("Bill", "Joe", "Sam");
			break;
		case "tran":
			break;
		case "insertEmployeeUsingSqlAnnotation":
			System.out.println("insertEmployeeWithRollbackAsDefault: employees found: 'Joe' and 'Sam'");
			assertEmployees("Joe", "Sam");
			break;
		default:
			throw new RuntimeException(
					"missing 'after transaction' assertion for test method: "
							+ method);
		}
	}

	private void assertEmployees(String... users) {
		List<String> expected = Arrays.asList(users);
		Collections.sort(expected);
		List<String> actual = jdbcTemplate.queryForList("select name from employee", String.class);
		Collections.sort(actual);
		System.out.println("Employees found: " + actual);
		assertEquals(expected, actual);
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;
}