package com.mk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.BatchUpdateException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class Application implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String args[]) {
		SpringApplication.run(Application.class, args);
	}

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Override
	public void run(String... strings) throws Exception {

		log.info("Creating tables");

		// jdbcTemplate.execute("DROP TABLE customers IF EXISTS ");
		jdbcTemplate.execute("DROP TABLE  IF EXISTS customers");

		// Last name is just 10 Char , so that we can recreate error in batch process
		jdbcTemplate.execute("CREATE TABLE customers(id SERIAL, first_name VARCHAR(255), last_name VARCHAR(10))");

		// Split up the array of whole names into an array of first/last names
		List<Object[]> splitUpNames = Arrays.asList("John Woo", "Jeff Dean", "Josh Bloch666666666666", "Josh Long")
				.stream().map(name -> name.split(" ")).collect(Collectors.toList());

		// Use a Java 8 stream to print out each tuple of the list
		splitUpNames.forEach(name -> log.info(String.format("Inserting customer record for %s %s", name[0], name[1])));

		// Uses JdbcTemplate's batchUpdate operation to bulk load data
		try {
			jdbcTemplate.batchUpdate("INSERT INTO customers(first_name, last_name) VALUES (?,?)", splitUpNames);
		} catch (Exception exp) {
			Throwable rootCause = exp.getCause();
			System.out.println("===========  Batch update failed= " + rootCause + "===========");
			if (rootCause instanceof BatchUpdateException) { //

				BatchUpdateException bue = (BatchUpdateException) rootCause;
				int lastSuccessfullRow = bue.getUpdateCounts().length;
				int failurePoint = lastSuccessfullRow + 1;
				int continuePoint = lastSuccessfullRow + 2;

				System.out.println("=========== Last successful row= " + lastSuccessfullRow + "===========");
				System.out.println("=========== Failed row= " + failurePoint + "===========");
				System.out.println("=========== continue point= " + continuePoint + "===========");

			} else {
				// if rootcause is not BatchUpdateException, then re-throw the exception
				System.out.println(
						"===========  rootcause is not BatchUpdateException, then re-throw the exception ===========");
				throw new Exception(exp);
			}
		}

		log.info("Querying for customer records where first_name = 'Josh':");
		jdbcTemplate.query("SELECT id, first_name, last_name FROM customers WHERE first_name = ?",
				new Object[] { "Josh" },
				(rs, rowNum) -> new Customer(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name")))
				.forEach(customer -> log.info(customer.toString()));
	}
}