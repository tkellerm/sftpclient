package de.abasgmbh.utils.sftp;


	import java.io.IOException;

	/** Stand-alone-Programm zum Download von einem SFTP-Server sowie zum Entpacken von .zip und .gz */
	public class DownloadFromSFtp
	{
	   public static void main( String[] args ) throws IOException
	   {
	      System.out.println( "\nDownload und Unzip von Dateien von einem SFTP-Server.\n"
	                  + "8 Parameter werden benoetigt:\n"
	                  + "  Quellverzeichnis auf dem Remote-Logserver, z.B.: /meinverzeichnis\n"
	                  + "  Zielerzeichnis im lokalen Dateisystem, z.B.: logs\n"
	                  + "  Teilstring der Bestandteil des Namens der herunterzuladenden Datei sein muss, z.B.: .log\n"
	                  + "  maximales Alter der Dateien in Tagen, z.B.: 7\n"
	                  + "  Benutzername\n"
	                  + "  Passwort\n"
	                  + "  Hostname\n"
	                  + "  Port, z.B.: 22\n" );
	      if( args == null || args.length < 8 ) {
	         System.out.println( "Fehler: Parameter fehlen." );
	         System.exit( 1 );
	      } else {
	         SFtpWrapper.downloadAndUnzip( args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7] );
	      }
	   }
	}

