package pigiadibooks.dbhandler;


/**
 * Classe Builder che costruisce le varie SQLCode occupandosi dei dettagli
 * I metodi sono autoesplicativi
 * 
 * @author Marco
 *
 */
public class SQLCodeBuilder {
	//creare metodi man mano che servono
	//statici?
	
	public static SQLCode createSelectAllFrom(String from){
		SQLCode toRet=new Query("SELECT * FROM "+from+";");
		return toRet;
	}
	
	public static SQLCode createSelectAllFromOrderBy(String from, String orderBy){
		SQLCode toRet=new Query("SELECT * FROM "+from
				+" ORDER BY "+orderBy+" ;");
		return toRet;
	}
	
	public static SQLCode createSelectAllFromWhere(String from, String where){
		SQLCode toRet=new Query("SELECT * FROM "+from
				+" WHERE "+where+" ;");
		return toRet;
	}
	
	public static SQLCode createSelectAllFromWhereOrderBy(String from, String where, String orderBy){
		SQLCode toRet=new Query("SELECT * FROM "+from
				+" WHERE "+where
				+" ORDER BY "+orderBy+" ;");
		return toRet;
	}
	
	public static SQLCode createSelectFromGroupByOrderBy(String columns,String from
			,String groupBy, String orderBy){
		SQLCode toRet=new Query("SELECT "+columns
				+" FROM "+from
				+" GROUP BY "+groupBy
				+" ORDER BY "+orderBy+" ;");
		return toRet;
	}
	
	public static SQLCode createSelectAllFromWhereWithParams(String from, String where, Object[] params){
		Query toRet=new Query("SELECT * FROM "+from
				+" WHERE "+where+" ;");
		for (Object object : params) {
			toRet.addParam(object);
		}
		return toRet;
	}
	
	public static SQLCode createSelectAllFromWhereOrderByWithParams(String from, String where
			,String orderBy, Object[] params){
		Query toRet=new Query("SELECT * FROM "+from
				+" WHERE "+where
				+" ORDER BY "+orderBy
				+" ;");
		for (Object object : params) {
			toRet.addParam(object);
		}
		return toRet;
	}
	
	public static SQLCode createInsertInto(String into, String[] columns, Object[][] values){
		//se non ho righei da inserire o colonne e valori per riga da inserire non corrispondono
		//ritorno null
		//altrimenti costruisco il DML
		if(values.length!=0 && (columns.length==0 || values[0].length==columns.length)){
			StringBuilder code=new StringBuilder("INSERT INTO "+into);
			if(columns.length!=0){
				code.append("(");
				for(String column:columns){
					code.append(column+",");
				}
				code.deleteCharAt(code.length()-1);
				code.append(") ");
			}
			else{
				code.append(" ");
			}
			code.append("VALUES (");
			for(int i=0;i<(values[0].length-1);i++){
				code.append("?,");
			}
			code.append("?);");
			DMLCode dml=new DMLCode(code.toString(), values.length, values[0].length);
			for(int i=0;i<values.length;i++){
				for(int j=0;j<values[0].length;j++){
					dml.setParam(i, j, values[i][j]);
				}	
			}
			return dml;
		}
		return null;
	}
	
	public static SQLCode createInsertIntoOnAllColumns(String into, Object[][] values){
		return createInsertInto(into, new String[]{}, values);
	}
	
	
	public static SQLCode createDeleteAllFrom(String from){
		return createDeleteFrom(from,new String[0],null,null);
	}
	
	public static SQLCode createDeleteFromOnlyEquals(String from, String[] whereColumns
						,Object[][] values){
		String[] operators=new String[whereColumns.length];
		for (int i = 0; i < whereColumns.length; i++) {
			operators[i]="=";
		}
		return createDeleteFrom(from,whereColumns,operators,values);
	}
	
	//solo AND!
	public static SQLCode createDeleteFrom(String from,String[] whereColumns
			,String[] operators, Object[][] values){
		StringBuilder code=new StringBuilder("DELETE FROM "+from);
		//se non ho where, faccio un cancella tutto
		if(whereColumns.length==0){
			return new DMLCode(code.toString()+";", 0, 0);
		}
		else{
			//se non ho le giuste corrispondenze sul numero di condizioni torno null
			//altrimenti creo la query
			if(whereColumns.length==operators.length 
					&& values.length!=0
					&& whereColumns.length==values[0].length){
				code.append(" WHERE ");
				for (int i = 0; i < (whereColumns.length-1); i++) {
					code.append(whereColumns[i]+operators[i]+"? AND ");
				}
				code.append(whereColumns[whereColumns.length-1]
						+operators[whereColumns.length-1]+"? ;");
				DMLCode dml=new DMLCode(code.toString(), values.length, values[0].length);
				for(int i=0;i<values.length;i++){
					for(int j=0;j<values[0].length;j++){
						dml.setParam(i, j, values[i][j]);
					}	
				}
				return dml;
			}
			else{
				return null;
			}
			
		}
	}
	
	public static SQLCode createUpdate(String table, String[] columnsToSet
			, Object[] values,String where){
		if(columnsToSet.length!=values.length){
			return null;
		}
		StringBuilder code=new StringBuilder("UPDATE "+table);
		code.append(" SET ");
		for(int i=0;i<(columnsToSet.length-1);i++){
			code.append(columnsToSet[i]+"=?,");
		}
		code.append(columnsToSet[(columnsToSet.length-1)]+"=? WHERE ");
		code.append(where+";");
		DMLCode update=new DMLCode(code.toString(), 1, values.length);
		for(int i=0;i<values.length;i++){
			update.setParam(0, i, values[i]);
		}
		return update;
	}

	
	
}
