Spring JDBC Template Batch Update
  * H2 Database not giving BatchUpdateException exception, i used MQSQL
  * 2 ways to process the batch are  with or without transaction.
  * @Transactional annotaion on method or class works fine, nothing else need to do.
  ```
String sql = "insert into person (id, name) " + "values(?,  ?)";  
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
		// BatchUpdateException helps to find out which job fail
		int lastSuccessfullRow = bue.getUpdateCounts().length;   
	}
}
```
