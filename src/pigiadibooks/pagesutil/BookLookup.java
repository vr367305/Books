package pigiadibooks.pagesutil;
import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;

import pigiadibooks.booklistsync.BookBeanStrategy;
import pigiadibooks.booklistsync.ExternalBookIndex;
import pigiadibooks.booklistsync.GoogleBooksIndex;
import pigiadibooks.dbhandler.DMLCode;
import pigiadibooks.dbhandler.MyDriver;
import pigiadibooks.dbhandler.Query;
import pigiadibooks.dbhandler.SQLCode;
import pigiadibooks.dbhandler.SQLCodeBuilder;
import pigiadibooks.model.BookModel;
import pigiadibooks.model.DataModel;

//TODO ottimizzare parallelizzando inserimenti finali o inserendo alcune chiamate nel costruttore

/**
 * Classe che interroga il DB e una sorgente esterna per ritornare risultati di ricerca in base al titolo
 * Si occupa di aggiungere al database i libri che non sono gi� presenti
 * 
 * @author Marco
 *
 */

public class BookLookup extends Observable implements Serializable{
	
	private BookBeanStrategy strat;
	private String title;
	
	public BookLookup(String title){
		this.strat=new BookBeanStrategy((Query) SQLCodeBuilder
				.createSelectAllFromWhereWithParams("ScrittoDa AS SD JOIN Libro L ON SD.libro_industryid=L.industryid "
				+ "JOIN Autore A ON A.nome=SD.autore_nome "
						, "L.titolo ILIKE ?"
						,new Object[]{"%"+title+"%"}));
		this.title=title;
		this.addObserver(new ResearchLoggerStat());
	}
	
	public BookLookup(){
		this.title=title;
		this.addObserver(new ResearchLoggerStat());
	}
	
	public String getTitle() {
		return title;
	}

	//TODO gestire il controllo in parallelo anche su Google Books oltre che sul db
	//magari metterlo nel costruttore che fa una thread che va a cercare.
	
	/**
	 * Torna i risultati della ricerca dati dall'unione dei risultati dal DB e di quelli dalla sorgente
	 * esterna. I risultati mancanti dal DB vengono aggiunti.
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public List<BookModel>  lookupByTitle() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, GeneralSecurityException, IOException{
		Set<BookModel> toRet=new LinkedHashSet<BookModel>();
		List<DataModel> resFromDB=this.strat.getSelectedBeans(MyDriver.getInstance().getConnection());
		for (DataModel db : resFromDB) {
			toRet.add((BookModel) db);
		}
		ExternalBookIndex ind=new GoogleBooksIndex();
		List<BookModel> fromGoogleBooks=ind.getBooksByTitle(this.title);
		this.insertBooks(fromGoogleBooks, MyDriver.getInstance().getConnection());
		toRet.addAll(fromGoogleBooks);
		
		//per statistiche ricerche libri
		//chiamo observer
		this.setChanged();
		this.notifyObservers(this.title);
		
		return new ArrayList<BookModel>(toRet);
	}
	
	
	/**
	 * Inserisci i libri dalla sorgente esterna. Usa l'opzione NO WARNINGS
	 * @param toIns
	 * @param c
	 * @throws SQLException
	 */
	private void insertBooks(List<BookModel> toIns,Connection c) throws SQLException{
		if(toIns.size()>0){
			Object[][] params=new Object[toIns.size()][5];
			for(int i=0;i<toIns.size();i++){
				BookModel bm=toIns.get(i);
				params[i][0]=bm.getindustryID();
				params[i][1]=bm.getTitolo();
				params[i][2]=bm.getDescrizione();
				params[i][3]=bm.getImgurl();
				params[i][4]=bm.getCategoria();
			}
			SQLCode insertBooks=SQLCodeBuilder.createInsertIntoOnAllColumns("Libro",params);
			
			
			Set<String> autori=new HashSet<String>();
			for (BookModel bm : toIns) {
				autori.addAll(bm.getAutori());
			}
			Object[][] paramsAutori=new Object[autori.size()][1];
			int iAut=0;
			for (String autore:autori) {
				paramsAutori[iAut][0]=autore;
				iAut++;
			}
			SQLCode insertAuthors=SQLCodeBuilder.createInsertIntoOnAllColumns("Autore", paramsAutori);
			
			
			
			List<String> wby_autori=new ArrayList<String>();
			List<String> wby_libri=new ArrayList<String>();
			for (BookModel bm : toIns) {
				for(String autore:bm.getAutori()){
					wby_libri.add(bm.getindustryID());
					wby_autori.add(autore);
				}
			}
			Object[][] paramsWBy=new Object[wby_autori.size()][2];
			for(int i=0;i<wby_autori.size();i++){
				paramsWBy[i][0]=wby_libri.get(i);
				paramsWBy[i][1]=wby_autori.get(i);
			}
			SQLCode insertWrittenBy=SQLCodeBuilder.createInsertIntoOnAllColumns("ScrittoDa", paramsWBy);
			
			if(insertBooks!=null){
				((DMLCode)insertBooks).setIgnoreWarnings(true);
				insertBooks.executeQueryOnConnection(c);
				if(insertAuthors!=null){
					((DMLCode)insertAuthors).setIgnoreWarnings(true);
					insertAuthors.executeQueryOnConnection(c);
				}
				if(insertWrittenBy!=null){
					((DMLCode)insertWrittenBy).setIgnoreWarnings(true);
					insertWrittenBy.executeQueryOnConnection(c);
				}
			}
		}
		
	}
	
	public BookModel getByID(String id) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		this.strat=new BookBeanStrategy((Query) SQLCodeBuilder
				.createSelectAllFromWhereWithParams("ScrittoDa AS SD JOIN Libro L ON SD.libro_industryid=L.industryid "
				+ "JOIN Autore A ON A.nome=SD.autore_nome "
						, "L.industryid=?"
						,new Object[]{id}));
		Set<BookModel> toRet=new HashSet<BookModel>();
		List<DataModel> resFromDB=this.strat.getSelectedBeans(MyDriver.getInstance().getConnection());
		for (DataModel db : resFromDB) {
			toRet.add((BookModel) db);
		}
		if(toRet.size()!=1){
			return new BookModel();
		}
		else{
			return new ArrayList<BookModel>(toRet).get(0);
		}
	}
}
