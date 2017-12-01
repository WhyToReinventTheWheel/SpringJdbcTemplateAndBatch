Spring JDBC Template Batch Update
  * H2 Database not giving BatchUpdateException exception, i used MQSQL
  * 2way to process the batch , with and without transaction.
  * @Transactional annotaion on method or class work fine, nothing else need to done.
  ```
  	try {
		resultArray = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			List<Person> list = new ArrayList<>();
			
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				Person person = list.get(i);
				ps.setInt(1, i);
				ps.setString(2, person.getName());
			}

			@Override
			public int getBatchSize() {
				return list.size();
			}
		});
	}catch (Exception exp){
		Throwable rootCause = exp.getCause();
		if(rootCause instanceof BatchUpdateException){
			BatchUpdateException bue = (BatchUpdateException)rootCause;
			// This help to find out which job fail 
			int lastSuccessfullRow = bue.getUpdateCounts().length;   
		}
	}
```
