package pigiadisoft.booklistsync;

import java.util.Arrays;

public class BookBean {

	String titolo,isbn_13;
	String[] autori;

	public BookBean() {
		this.titolo = "";
		this.autori = new String[0];
		this.isbn_13 = "";
	}

	public BookBean(String titolo, String[] autori, String isbn_13) {
		this.titolo = titolo;
		this.autori = autori.clone();
		this.isbn_13 = isbn_13;
	}

	public String getTitolo() {
		return titolo;
	}

	public void setTitolo(String titolo) {
		this.titolo = titolo;
	}

	

	public String[] getAutori() {
		return autori;
	}

	public void setAutori(String[] autori) {
		this.autori = autori;
	}

	public String getIsbn_13() {
		return isbn_13;
	}

	public void setIsbn_13(String isbn_13) {
		//conversione automatica da formato isbn10 a formato isbn13
		if(isbn_13.length()==10){
			this.isbn_13 = ISBN10toISBN13(isbn_13);
		}
		else{
			this.isbn_13=isbn_13;
		}
		
	}
	
	private static String ISBN10toISBN13(String ISBN10) {
		String ISBN13 = ISBN10;
		ISBN13 = "978" + ISBN13.substring(0, 9);
		// if (LOG_D) Log.d(TAG, "ISBN13 without sum" + ISBN13);
		int d;

		int sum = 0;
		for (int i = 0; i < ISBN13.length(); i++) {
			d = ((i % 2 == 0) ? 1 : 3);
			sum += ((((int) ISBN13.charAt(i)) - 48) * d);
			// if (LOG_D) Log.d(TAG, "adding " + ISBN13.charAt(i) + "x" + d +
			// "=" + ((((int) ISBN13.charAt(i)) - 48) * d));
		}
		sum = 10 - (sum % 10);
		ISBN13 += sum;

		return ISBN13;
	}

	@Override
	public String toString() {
		return "BookBean [titolo=" + titolo + ", isbn_13=" + isbn_13
				+ ", autori=" + Arrays.toString(autori) + "]";
	}
	
	
	
	

}