package de.abasgmbh.utils.sftp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import de.abas.eks.jfop.FOPException;
import de.abas.eks.jfop.remote.ContextRunnable;
import de.abas.eks.jfop.remote.FOPSessionContext;
import de.abas.erp.api.gui.TextBox;
import de.abas.erp.common.AbasException;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.settings.DisplayMode;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.UserTextBuffer;

public class abas2SFTP implements ContextRunnable {
	
//	private static Path helpFile = new P"documents/help.txt"; 
			
	private Logger logger = Logger.getLogger(abas2SFTP.class);
	
	private static final String helpFile = "java/projects/sftpclient/documents/help.txt";
	private static String varnameHost = "xjthost";
	private static String varnamePort = "xjtport";
	private static String varnameUser = "xjtuser";
	private static String varnamePassword = "xjtpassword";
	private static String varnameLocalFile = "xjtlocalfile";
	private static String varnameRemoteFile = "xjtremotefile";
	private static String varnameErrorMessage = "xjterrormessage";
	private static String varnameMessage = "xjtmessage";
	private static String varnameCommand = "xjtcommand";
	private static String varnameHelp = "xjthelp";
	
	private String host;
	private String port;
	private String username;
	private String password;
	private String localeFileName;
	private String remoteFileName;
	private String command;
	private String message;
//	private String errormessage;

	
	
	@Override
	public int runFop(FOPSessionContext ctx, String[] args) throws FOPException {
		
		DbContext dbContext = ctx.getDbContext();
		
//		BasicConfigurator.configure();
		
	    return run(dbContext, args);	    
	}

	 private Integer run(DbContext dbContext, String[] args){
		 
		/**
		 * Es soll 2 verschiedene Wege für den Aufruf geben
		 * zum einen über eigene U|variablen und der andere Weg über die übergebenen Argumente.
		 * Wobei die variablen Vorang haben
		 * 
		 */
		 
			BufferFactory bufferFactory = BufferFactory.newInstance(true);
			UserTextBuffer userTextBuffer = bufferFactory.getUserTextBuffer();

			int anzahlargs = args.length;
			
//			Indikator, ob über die Argumente oder die Variablen verfahren wird ist die Variable hosts
			
			definedUsertextvars(userTextBuffer);
			
			if (!userTextBuffer.isEmpty(varnameHost)) {
				host = userTextBuffer.getStringValue(varnameHost);
				port = userTextBuffer.getStringValue(varnamePort);
				username = userTextBuffer.getStringValue(varnameUser);
				password = userTextBuffer.getStringValue(varnamePassword);
				localeFileName = userTextBuffer.getStringValue(varnameLocalFile);
				remoteFileName = userTextBuffer.getStringValue(varnameRemoteFile);
				command = userTextBuffer.getStringValue(varnameCommand);
				
				try {
					sftpKommandos(host , port , username , password , localeFileName , remoteFileName , command);
					logger.info("var Host:" + host + " Port:" + port + " User:" + username + " LocalFile:" + localeFileName + " Remote:" + remoteFileName + " Command" + command );
					variable2textbuffer(this.message , varnameMessage, userTextBuffer);
				}  catch (NumberFormatException e) {
					
					logger.error("var Host:" + host + " Port:" + port + " User:" + username + " LocalFile:" + localeFileName + " Remote:" + remoteFileName + " Command" + command , e);
					exeptionToErrorMessage(e, userTextBuffer);
					return 1;
				} catch (IOException e) {
					logger.error("var Host:" + host + " Port:" + port + " User:" + username + " LocalFile:" + localeFileName + " Remote:" + remoteFileName + " Command" + command , e);
					exeptionToErrorMessage(e, userTextBuffer);
					return 1;
				}  catch (AbasException e) {
					logger.error("args7 Host:" + host + " Port:" + port + " User:" + username + " LocalFile:" + localeFileName + " Remote:" + remoteFileName + " Command" + command , e);
					exeptionToErrorMessage(e, userTextBuffer);
					return 1;
				}
				
				
			}else if (anzahlargs > 1) {
//				Prüfung auf das erste Argument
				
//				Kommando Server Port Username Passwort RemoteFile LokalFile 
				command = args[1];
				if(anzahlargs == 8){
					host = args[2];
					port = args[3];
					username = args[4];
					password  = args[5];
					remoteFileName = args[6];
					localeFileName = args[7];
					try {
						sftpKommandos(host , port , username , password , localeFileName , remoteFileName , command);
						logger.info("args8 Host:" + host + " Port:" + port + " User:" + username + " LocalFile:" + localeFileName + " Remote:" + remoteFileName + " Command" + command );
						variable2textbuffer(this.message , varnameMessage, userTextBuffer);
					}  catch (NumberFormatException e) {
						logger.error("args8 Host:" + host + " Port:" + port + " User:" + username + " LocalFile:" + localeFileName + " Remote:" + remoteFileName + " Command" + command , e);
						exeptionToErrorMessage(e, userTextBuffer);
						return 1;
					} catch (IOException e) {
						logger.error("args8 Host:" + host + " Port:" + port + " User:" + username + " LocalFile:" + localeFileName + " Remote:" + remoteFileName + " Command" + command , e);
						exeptionToErrorMessage(e, userTextBuffer);
						return 1;
					} catch (AbasException e) {
						logger.error("args7 Host:" + host + " Port:" + port + " User:" + username + " LocalFile:" + localeFileName + " Remote:" + remoteFileName + " Command" + command , e);
						exeptionToErrorMessage(e, userTextBuffer);
						return 1;
					}	
					
				}else if (command.equals("listRemFiles")  && anzahlargs == 7) {
					host = args[2];
					port = args[3];
					username = args[4];
					password  = args[5];
					remoteFileName = args[6];
					localeFileName = "";
					try {
						sftpKommandos(host , port , username , password , localeFileName , remoteFileName , command);
						logger.info("args7 Host:" + host + " Port:" + port + " User:" + username + " LocalFile:" + localeFileName + " Remote:" + remoteFileName + " Command" + command );
						variable2textbuffer(this.message , varnameMessage, userTextBuffer);
						return 0;
					}  catch (NumberFormatException e) {
						logger.error("args7 Host:" + host + " Port:" + port + " User:" + username + " LocalFile:" + localeFileName + " Remote:" + remoteFileName + " Command" + command , e);
						exeptionToErrorMessage(e, userTextBuffer);
						return 1;
					} catch (IOException e) {
						logger.error("args7 Host:" + host + " Port:" + port + " User:" + username + " LocalFile:" + localeFileName + " Remote:" + remoteFileName + " Command" + command , e);
						exeptionToErrorMessage(e, userTextBuffer);
						return 1;
					} catch (AbasException e) {
						logger.error("args7 Host:" + host + " Port:" + port + " User:" + username + " LocalFile:" + localeFileName + " Remote:" + remoteFileName + " Command" + command , e);
						exeptionToErrorMessage(e, userTextBuffer);
						return 1;
					}
				}else {
					TextBox textbox = new TextBox(dbContext, "Fehler", "Es wurden zu wenige Aufrufparameter übergeben");
					textbox.show();
					return 1;
				}
				
				
				
			}else {
//				Es wurden keine Variablen und keine Argumente übergeben, dann wird die Hilfe als Text angezeigt.	
				helpToScreen(dbContext);
				return 0;
			}
			
			return 0;
			
			
			
	 }

	 private void exeptionToErrorMessage(Exception e, UserTextBuffer userTextBuffer) {
		 
		 if (!userTextBuffer.isVarDefined(varnameErrorMessage)) {
				userTextBuffer.defineVar("TEXT", varnameErrorMessage);
			}
		 
			userTextBuffer.assign(varnameErrorMessage, e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e) + "\n");
		
	}

	private void variable2textbuffer(String textvariable , String variablenname , UserTextBuffer userTextBuffer){
		
		if (!userTextBuffer.isVarDefined(variablenname)) {
			userTextBuffer.defineVar("TEXT", variablenname);
		}
	 
		userTextBuffer.assign(variablenname, textvariable);
		
	}
	 
	/**
	  * 
	  * über den Parameter Kommando werden die einzelnen Aktionen ausgelöst
	  * 
	  * @param host
	  * @param port
	  * @param username
	  * @param password
	  * @param localeFileName
	  * @param remoteFileName
	  * @param command
	  * 
	  * putDir  - Alle Dateien eines Verzeichnisses auf den Server kopieren
	  * putFile - Eine Datei auf den Server kopieren
	  * mv2remDir   - Alle Dateien eines Verzeichnisses auf den Server kopieren und anschließend lokal löschen
  	  * mv2remFile  - Eine Datei auf den Server kopieren und anschließend lokal löschen
      *
      * getDir  - Alle Dateien eines Verzeichnisses von einem Server in ein lokales Verzeichnis kopieren
      * getFile - Eine Datei  von einem Server in ein lokales Verzeichnis kopieren
      * mv2locDir - Alle Dateien eines Verzeichnisses von einem Server in eine lokales Verzeichnis kopieren und anschließend auf dem Server löschen
      * mv2locFile - Eine Dateie von einem Server in eine lokales Verzeichnis kopieren und anschließend auf dem Server löschen
      *
      * listRemFiles
	 * @throws AbasException 
	 * @throws IOException 
	 * @throws NumberFormatException 
	  */
	 
	private void sftpKommandos(String host, String port, String username,
			String password, String localeFileName, String remoteFileName,
			String command) throws  NumberFormatException, IOException, AbasException {
		
		switch (command) {
		case "putDir":

				SFtpWrapper.uploadDirectory(remoteFileName, localeFileName, username, password, host, port);			
			break;
		case "putFile" :
			
				SFtpWrapper.uploadFile(remoteFileName, localeFileName, username, password, host, port);
			
			break;
		case "mv2remDir" :
			
				SFtpWrapper.uploadDirectoryAndRemoveFiles(remoteFileName, localeFileName, username, password, host, port);
			
			break;
		case "mv2remFile" :
			
				SFtpWrapper.uploadFileAndRemove(remoteFileName, localeFileName, username, password, host, port);
			
			break;
		case "getDir" :
			
				SFtpWrapper.downloadFilesInDirectory(remoteFileName, localeFileName, username, password, host, port);
			
			break;
		case "getFile" :
			
				SFtpWrapper.downloadFile(remoteFileName, localeFileName, username, password, host, port);
			
			break;
		case "mv2locDir" :
			
				SFtpWrapper.downloadAndRemoveFilesInDirectory(remoteFileName, localeFileName, username, password, host, port);
						
			break;
		case "mv2locFile" :
			
				SFtpWrapper.downloadFileAndRemove(remoteFileName, localeFileName, username, password, host, port);
			
			break;
		case "listRemFiles" :
			
				this.message = SFtpWrapper.getFileDataListOnlyFilename(remoteFileName, username, password, host, port);
			
			break;
			
		default:
//			Bei falschem Commando wird eine Exeption geworfen
			throw new AbasException("Das Kommando " + command + " ist unbekannt!");
			
		}

		
	}
	
	private void definedUsertextvars(UserTextBuffer userTextBuffer){
		
		if (!userTextBuffer.isVarDefined(varnameHost)) {
			userTextBuffer.defineVar("TEXT", varnameHost);
		}
		
		if (!userTextBuffer.isVarDefined(varnamePort)) {
			userTextBuffer.defineVar("TEXT", varnamePort);
		}
		
		if (!userTextBuffer.isVarDefined(varnameHost)) {
			userTextBuffer.defineVar("TEXT", varnameHost);
		}
		
		if (!userTextBuffer.isVarDefined(varnameUser)) {
			userTextBuffer.defineVar("TEXT", varnameUser);
		}
		
		if (!userTextBuffer.isVarDefined(varnamePassword)) {
			userTextBuffer.defineVar("TEXT", varnamePassword);
		}
		
		if (!userTextBuffer.isVarDefined(varnameLocalFile)) {
			userTextBuffer.defineVar("TEXT", varnameLocalFile);
		}
		
		if (!userTextBuffer.isVarDefined(varnameRemoteFile)) {
			userTextBuffer.defineVar("TEXT", varnameRemoteFile);
		}
		
		if (!userTextBuffer.isVarDefined(varnameErrorMessage)) {
			userTextBuffer.defineVar("TEXT", varnameErrorMessage);
		}
		
		if (!userTextBuffer.isVarDefined(varnameCommand)) {
			userTextBuffer.defineVar("TEXT", varnameCommand);
		}
		
		if (!userTextBuffer.isVarDefined(varnameHelp)) {
			userTextBuffer.defineVar("TEXT", varnameHelp);
		}
		
	}

	
	private void helpToScreen(DbContext dbContext) {
		
		dbContext.getSettings().setDisplayMode(DisplayMode.DISPLAY);
		
		PrintWriter dbwriter = dbContext.out();


		
		try {
					
			FileReader fr = new FileReader(helpFile);
		     BufferedReader br = new BufferedReader(fr);

		     String zeile = "";

		     while( (zeile = br.readLine()) != null )
		     {
					dbwriter.println(zeile);
		     }

		     br.close();

		} catch (IOException e) {
			TextBox textbox = new TextBox(dbContext, "Fehler" , "Fehler beim Lesen der Hilfe-Datei" + e.toString());
			textbox.show();
		}

	}
	
	
}



