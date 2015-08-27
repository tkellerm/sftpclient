package de.abasgmbh.utils.sftp;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.*;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.sun.org.apache.xpath.internal.axes.LocPathIterator;

/** Wrapper-Klasse zur Dateiuebertragung von und zu einem SFTP-Server sowie zum Entpacken von .zip und .gz.
    Die Wrapper-Klasse dient zur Entkopplung von der SFTP-Implementierung. */
public class SFtpWrapper implements AutoCloseable
{
   private Session     session;
   private ChannelSftp channel;

   public SFtpWrapper( String benutzername, String passwort, String host, int port ) throws IOException
   {
      try {
         session = (new JSch()).getSession( benutzername, host, port );
         session.setPassword( passwort );
         session.setConfig( "StrictHostKeyChecking", "no" );
         session.connect();
      } catch( JSchException ex ) {
         throw new IOException( "Fehler beim SFTP-Connect mit '" + benutzername + "' an '" + host + "': ", ex );
      }
      try {
         channel = (ChannelSftp) session.openChannel( "sftp" );
         if( channel == null ) {
            close();
            throw new IOException( "Fehler beim Oeffnen des SFTP-Channel zur SFTP-Session mit '" + session.getUserName() + "' an '" + session.getHost() + "'. " );
         }
         channel.connect();
      } catch( JSchException ex ) {
         close();
         throw new IOException( "Fehler beim Oeffnen des SFTP-Channel zur SFTP-Session mit '" + session.getUserName() + "' an '" + session.getHost() + "': ", ex );
      }
   }

   @Override
   public void close()
   {
      try {
         if( channel != null ) {
            channel.disconnect();
            channel = null;
         }
      } finally {
         if( session != null ) {
            session.disconnect();
            session = null;
         }
      }
   }

   public String getLocalActualDir()
   {
      return channel.lpwd();
   }

   public String getRemoteActualDir() throws IOException
   {
      try {
         return channel.pwd();
      } catch( SftpException ex ) {
         throw new IOException( ex );
      }
   }

   /** Datei-Daten zu einer einzelnen Datei */
   public FileData getFileData( String remoteFilePath ) throws IOException
   {
      try {
         @SuppressWarnings("unchecked")
         List<ChannelSftp.LsEntry> lsEntryLst = channel.ls( remoteFilePath );
         if( lsEntryLst == null || lsEntryLst.size() != 1 ) {
            return null;
         }
         LsEntry lsEntry = lsEntryLst.get( 0 );
         FileData fd = new FileData();
         int i = remoteFilePath.lastIndexOf( '/' );
         fd.parentPath  = ( i < 0 ) ? "" : remoteFilePath.substring( 0, i );
         fd.isDirectory =  lsEntry.getAttrs().isDir();
         fd.isFile      = !lsEntry.getAttrs().isDir() && !lsEntry.getAttrs().isLink();
         fd.name        = lsEntry.getFilename();
         fd.size        = lsEntry.getAttrs().getSize();
         fd.timestamp   = Calendar.getInstance();
         fd.timestamp.setTimeInMillis( 1000L * lsEntry.getAttrs().getMTime() );
         return fd;
      } catch( SftpException ex ) {
         throw new IOException( ex );
      }
   }

   /** Datei-Daten zu allen Dateien in einem Verzeichnis */
   public List<FileData> getFileDataList( String remoteDir ) throws IOException
   {
      try {
         List<FileData> fileDataLst = new ArrayList<FileData>();
         @SuppressWarnings("unchecked")
         List<ChannelSftp.LsEntry> lsEntryLst = channel.ls( remoteDir );
         for( LsEntry lsEntry : lsEntryLst ) {
            FileData fd = new FileData();
            fd.parentPath  = remoteDir;
            fd.isDirectory =  lsEntry.getAttrs().isDir();
            fd.isFile      = !lsEntry.getAttrs().isDir() && !lsEntry.getAttrs().isLink();
            fd.name        = lsEntry.getFilename();
            fd.size        = lsEntry.getAttrs().getSize();
            fd.timestamp   = Calendar.getInstance();
            fd.timestamp.setTimeInMillis( 1000L * lsEntry.getAttrs().getMTime() );
            fileDataLst.add( fd );
         }
         return fileDataLst;
      } catch( SftpException ex ) {
         throw new IOException( ex );
      }
   }

   public void createRemoteFile( InputStream is, String remoteDstFilePath ) throws IOException
   {
      try {
         channel.put( is, remoteDstFilePath );
      } catch( SftpException ex ) {
         throw new IOException( ex );
      }
   }

   public void uploadFile( String localSrcFilePath, String remoteDstFilePath ) throws IOException
   {
      try {
         channel.put( localSrcFilePath, remoteDstFilePath );
      } catch( SftpException ex ) {
         throw new IOException( ex );
      }
   }

   public void downloadFile( String remoteSrcFilePath, String localDstFilePath ) throws IOException
   {
      try {
         channel.get( remoteSrcFilePath, localDstFilePath );
      } catch( SftpException ex ) {
         throw new IOException( ex );
      }
   }
   
   /** löschen einer Datei auf  remote */
   
   public void deleteFile( String remoteSrcFilePath) throws IOException
   {
      try {
         channel.rm( remoteSrcFilePath );
      } catch( SftpException ex ) {
         throw new IOException( ex );
      }
   }

   public void deleteLocalFile(String localFile){
	   
	   File file = new File(localFile);
	   if (file.isFile()) {
		file.delete();
	   }
   }
   
   
   
   /** Entpacken von remote .gz */
   public void ungzipRemote( String remoteSourceZipFile, String localDestFilePath ) throws IOException
   {
      try( InputStream instreamZipped = channel.get( remoteSourceZipFile ) ) {
         ungzipStream( instreamZipped, localDestFilePath );
      } catch( Exception ex ) {
         throw new IOException( "Fehler beim Unzip von " + remoteSourceZipFile + ",", ex );
      }
   }

   /** Entpacken von lokalem .gz */
   public static void ungzipLocal( String localSourceZipFile, String localDestFilePath ) throws IOException
   {
      try( InputStream instreamZipped = new FileInputStream( localSourceZipFile ) ) {
         ungzipStream( instreamZipped, localDestFilePath );
      } catch( Exception ex ) {
         throw new IOException( "Fehler beim Unzip von " + localSourceZipFile + ",", ex );
      }
   }

   /** Entpacken von .gz */
   public static void ungzipStream( InputStream instreamZipped, String localDestFilePath ) throws IOException
   {
      try( GZIPInputStream zin = new GZIPInputStream( new BufferedInputStream( instreamZipped ) ) ) {
         try( BufferedOutputStream os = new BufferedOutputStream( new FileOutputStream( localDestFilePath ) ) ) {
            int size;
            byte[] buffer = new byte[64 * 1024];
            while( (size = zin.read( buffer, 0, buffer.length )) > 0 ) {
               os.write( buffer, 0, size );
            }
         }
      }
   }

   /** Entpacken von remote .zip */
   public long unzipRemote( String remoteSourceZipFile, String localDestDir ) throws IOException
   {
      try( InputStream instreamZipped = channel.get( remoteSourceZipFile ) ) {
         return unzipStream( instreamZipped, localDestDir );
      } catch( Exception ex ) {
         throw new IOException( "Fehler beim Unzip von " + remoteSourceZipFile + ",", ex );
      }
   }

   /** Entpacken von lokalem .zip */
   public static long unzipLocal( String localSourceZipFile, String localDestDir ) throws IOException
   {
      try( InputStream instreamZipped = new FileInputStream( localSourceZipFile ) ) {
         return unzipStream( instreamZipped, localDestDir );
      } catch( Exception ex ) {
         throw new IOException( "Fehler beim Unzip von " + localSourceZipFile + ",", ex );
      }
   }

   /** Entpacken von .zip */
   public static long unzipStream( InputStream instreamZipped, String localDestDir ) throws IOException
   {
      long   anzahlEntries = 0;
      String remoteResultFilename = null;
      String destDir = ( localDestDir == null ) ? "" : localDestDir.trim();
      destDir = ( destDir.endsWith( "/" ) || destDir.endsWith( "\\" ) ) ? destDir : (destDir + File.separator);
      try( ZipInputStream zin = new ZipInputStream( new BufferedInputStream( instreamZipped ) ) ) {
         ZipEntry zipEntry;
         while( (zipEntry = zin.getNextEntry()) != null ) {
            remoteResultFilename = zipEntry.getName();
            if( remoteResultFilename != null && remoteResultFilename.startsWith( "/" ) && remoteResultFilename.length() > 1 ) {
               remoteResultFilename = remoteResultFilename.substring( 1 );
            }
            try( BufferedOutputStream os = new BufferedOutputStream( new FileOutputStream( destDir + remoteResultFilename ) ) ) {
               int size;
               byte[] buffer = new byte[64 * 1024];
               while( (size = zin.read( buffer, 0, buffer.length )) > 0 ) {
                  os.write( buffer, 0, size );
               }
            }
            zin.closeEntry();
            anzahlEntries++;
         }
      } catch( Exception ex ) {
         throw new IOException( "Fehler beim Unzip, letzter Zip-Entry " + remoteResultFilename + ",", ex );
      }
      return anzahlEntries;
   }

   /** Downloaden sowie Entpacken von .zip und .gz */
   public void downloadAndUnzip( String remoteSrcDir, String localDstDir, String filenameMustContain, int maxAlterInTagen ) throws IOException
   {
      Calendar cal = new GregorianCalendar();
      cal.add( Calendar.DAY_OF_MONTH, -1 * maxAlterInTagen );
      (new File( localDstDir )).mkdirs();
      List<FileData> fds = getFileDataList( remoteSrcDir );
      for( FileData fd : fds ) {
         if( fd.isFile && fd.name.contains( filenameMustContain ) && fd.timestamp.after( cal ) ) {
            String remoteSrcFilePath = fd.parentPath + "/" + fd.name;
            String localDstFilePath  = localDstDir + File.separator + fd.name;
            if( fd.name.toLowerCase().endsWith( ".zip" ) ) {
               unzipRemote( remoteSrcFilePath, localDstDir );
            } else if( fd.name.toLowerCase().endsWith( ".gz" ) ) {
               ungzipRemote( remoteSrcFilePath, localDstFilePath.substring( 0, localDstFilePath.length() - 3 ) );
            } else {
               downloadFile( remoteSrcFilePath, localDstFilePath );
            }
         }
      }
   }

   /** Downloaden sowie Entpacken von .zip und .gz */
   public static void downloadAndUnzip( String remoteSrcDir, String localDstDir, String filenameMustContain, String maxAlterInTagen,
                                        String benutzername, String passwort, String host, String port ) throws IOException
   {
      try( SFtpWrapper sftpWrapper = new SFtpWrapper( benutzername, passwort, host, Integer.parseInt( port ) ) ) {
         SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
         System.out.println( "Remote in " + remoteSrcDir + ":" );
         List<FileData> fds = sftpWrapper.getFileDataList( remoteSrcDir );
         for( FileData fd : fds ) {
            if( fd.isFile ) {
               System.out.println( df.format( Long.valueOf( fd.timestamp.getTimeInMillis() ) ) + ", " + fd.size + " Bytes, " + fd.name );
            }
         }
         sftpWrapper.downloadAndUnzip( remoteSrcDir, localDstDir, filenameMustContain, Integer.parseInt( maxAlterInTagen ) );
         System.out.println( "Lokal in " + localDstDir + ":" );
         File[] fls = (new File( localDstDir )).listFiles();
         for( File fl : fls ) {
            System.out.println( df.format( Long.valueOf( fl.lastModified() ) ) + ", " + fl.length() + " Bytes, " + fl.getName() );
         }
      }
   }

	public static void downloadAndRemoveFilesInDirectory(String remoteSrcDir, String localDstDir, String benutzername, String passwort,	String host, String port) throws IOException
	
	{
		SFtpWrapper sftpWrapper = new SFtpWrapper(benutzername, passwort, host, Integer.parseInt(port)); 
			
		sftpWrapper.downloadAndRemoveFilesInDirectory(remoteSrcDir, localDstDir);
			
		
	}
   
   
   public void downloadAndRemoveFilesInDirectory( String remoteSrcDir, String localDstDir ) throws IOException
   {
      
      (new File( localDstDir )).mkdirs();
      
      List<FileData> fds = getFileDataList( remoteSrcDir );
      for( FileData fd : fds ) {
         if( fd.isFile ) {
            String remoteSrcFilePath = fd.parentPath + "/" + fd.name;
            String localDstFilePath  = localDstDir + File.separator + fd.name;
               downloadFile( remoteSrcFilePath, localDstFilePath );
               if ((new File(localDstFilePath)).exists()) {
            	   deleteFile(remoteSrcFilePath);
			}
         }
      }
   }
   
   
   public static void downloadFilesInDirectory(String remoteSrcDir, String localDstDir, String benutzername, String passwort,	String host, String port) throws IOException
	
	{
		SFtpWrapper sftpWrapper = new SFtpWrapper(benutzername, passwort, host, Integer.parseInt(port)); 
			
		sftpWrapper.downloadAndRemoveFilesInDirectory(remoteSrcDir, localDstDir);
			
		
	}
   
   public void downloadFilesInDirectory( String remoteSrcDir, String localDstDir ) throws IOException
   {
      
      (new File( localDstDir )).mkdirs();
      
      List<FileData> fds = getFileDataList( remoteSrcDir );
      for( FileData fd : fds ) {
         if( fd.isFile ) {
            String remoteSrcFilePath = fd.parentPath + "/" + fd.name;
            String localDstFilePath  = localDstDir + File.separator + fd.name;
               downloadFile( remoteSrcFilePath, localDstFilePath );
             
         }
      }
   }
   
   public static void downloadFile(String remoteSrcFile, String localDstDir, String benutzername, String passwort,	String host, String port) throws IOException
	
	{
		SFtpWrapper sftpWrapper = new SFtpWrapper(benutzername, passwort, host, Integer.parseInt(port)); 
			
		sftpWrapper.downloadFile(remoteSrcFile, localDstDir);
		
	}
   
   public static void downloadFileAndRemove(String remoteSrcFile, String localDstDir, String benutzername, String passwort,	String host, String port) throws IOException
	
	{
		SFtpWrapper sftpWrapper = new SFtpWrapper(benutzername, passwort, host, Integer.parseInt(port)); 
			
		sftpWrapper.downloadFile(remoteSrcFile, localDstDir);
		
		  if ((new File(localDstDir)).exists()) {
			  sftpWrapper.deleteFile(remoteSrcFile);
		}
		
		
	}
  
   public static void uploadFile(String remoteDestFile, String localFile, String benutzername, String passwort, String host, String port) throws IOException
	
  	{
  		SFtpWrapper sftpWrapper = new SFtpWrapper(benutzername, passwort, host, Integer.parseInt(port)); 
  		sftpWrapper.uploadFile(localFile, remoteDestFile);
  		  		
  	}
   
   public static void uploadDirectory(String remoteDestDirectory, String localDirectory, String benutzername, String passwort, String host, String port) throws NumberFormatException, IOException 
   {
	   SFtpWrapper sftpWrapper = new SFtpWrapper(benutzername, passwort, host, Integer.parseInt(port));
	   File localDir = new File(localDirectory);
	   String localSrcFilePath = "";
	   String remoteDestfile = "";
	   if (localDir.isDirectory()) {
		
		File[] listFiles = localDir.listFiles();
		for (File file : listFiles) {
			if (file.isFile()) {
				
				localSrcFilePath = file.getAbsolutePath() ;
				remoteDestfile = remoteDestDirectory +  file.getName();
				sftpWrapper.uploadFile(localSrcFilePath, remoteDestDirectory);
				FileData fileData = sftpWrapper.getFileData(remoteDestfile);
				
			}
			
		}
		
	}
	   
   }
   
   
   
   public static void uploadDirectoryAndRemoveFiles(String remoteDestDirectory, String localDirectory, String benutzername, String passwort, String host, String port) throws NumberFormatException, IOException 
   {
	   SFtpWrapper sftpWrapper = new SFtpWrapper(benutzername, passwort, host, Integer.parseInt(port));
	   File localDir = new File(localDirectory);
	   String localSrcFilePath = "";
	   String remoteDestfile = "";
	   if (localDir.isDirectory()) {
		
		File[] listFiles = localDir.listFiles();
		for (File file : listFiles) {
			if (file.isFile()) {
				
				localSrcFilePath = file.getAbsolutePath() + "\\" + file.getName();
				remoteDestfile = remoteDestDirectory + "\\" + file.getName();
				sftpWrapper.uploadFile(localSrcFilePath, remoteDestDirectory);
				FileData fileData = sftpWrapper.getFileData(remoteDestfile);
		 		if (fileData != null) {
		 			if (fileData.size > 0) {
		 				sftpWrapper.deleteLocalFile(localSrcFilePath);
					}
					
				}
				
			}
			
		}
		
	}
	   
   }
   
   public static void uploadFileAndRemove(String remoteDestFile, String localFile, String benutzername, String passwort, String host, String port) throws IOException
	
 	{
 		SFtpWrapper sftpWrapper = new SFtpWrapper(benutzername, passwort, host, Integer.parseInt(port)); 
 		sftpWrapper.uploadFile(localFile, remoteDestFile);
 		FileData fileData = sftpWrapper.getFileData(remoteDestFile);
 		if (fileData != null) {
 			if (fileData.size > 0) {
 				sftpWrapper.deleteLocalFile(localFile);
			}
			
		}
 		
 	}
   
   
   
  public void downloadFiles( String remoteSrcFile, String localDstDir ) throws IOException
  {
     
     (new File( localDstDir )).mkdirs();
     
     List<FileData> fds = getFileDataList( remoteSrcFile );
     for( FileData fd : fds ) {
        if( fd.isFile ) {
           String remoteSrcFilePath = fd.parentPath + "/" + fd.name;
           String localDstFilePath  = localDstDir + File.separator + fd.name;
              downloadFile( remoteSrcFilePath, localDstFilePath );
              
        }
     }
  }
   
  private String getFileDataListOnlyFilename( String remoteDir ) throws IOException {
	  
	  List<FileData> fileDataList = getFileDataList(remoteDir);
	  String fileList = ""; 
	  for (FileData fileData : fileDataList) {
		fileList = fileList + fileData.name + "\n";
	}
	  return fileList;
  }
   
  public static String getFileDataListOnlyFilename(String remoteSrcFile, String benutzername, String passwort,	String host, String port) throws IOException
	
	{
		SFtpWrapper sftpWrapper = new SFtpWrapper(benutzername, passwort, host, Integer.parseInt(port)); 
		return sftpWrapper.getFileDataListOnlyFilename(remoteSrcFile);
		
	}
  
   /** Datei-Daten */
   public static class FileData
   {
      public boolean  isFile;
      public boolean  isDirectory;
      public String   parentPath;
      public String   name;
      public long     size;
      public Calendar timestamp;
   }
}
