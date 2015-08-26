package test.de.abasgmbh.utils.sftp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.sshd.SshServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.abasgmbh.utils.sftp.SFtpWrapper;
import de.abasgmbh.utils.sftp.SFtpWrapper.FileData;


public class SFtpWrapperTest
{
   private static final String BENUTZERNAME = "usr";
   private static final String PASSWORT     = "pwd";
   private static final String HOST         = "localhost";
   private static final int    PORT         = 14022;
   private static SshServer sftpServer;

   @BeforeClass
   public static void startSFtpServer() throws IOException
   {
      sftpServer = SFtpTestUtil.startSFtpServer( BENUTZERNAME, PASSWORT, PORT );
   }

   @AfterClass
   public static void stoppSFtpServer() throws InterruptedException
   {
      sftpServer.stop();
   }

   @Test
   public void testSFtpWrapper() throws IOException
   {
      String localSftpSubdir  = "target/sftp-local/";
      String remoteSftpSubdir = "target/sftp-remote/";
      String localFilePathB   = localSftpSubdir  + "b.txt";
      String remoteFilePathA  = remoteSftpSubdir + "a.txt";
      String remoteFilePathC  = remoteSftpSubdir + "c.txt";
      String meinText         = "äöüß\u20AC"; // \u20AC = Euro-Zeichen
      String charEncoding     = "UTF-8";
      (new File( localSftpSubdir  )).mkdirs();
      (new File( remoteSftpSubdir )).mkdirs();

      try( SFtpWrapper sftpWrapper = new SFtpWrapper( BENUTZERNAME, PASSWORT, HOST, PORT ) ) {
    	  String test = sftpWrapper.getLocalActualDir();
    	  String test2 = sftpWrapper.getRemoteActualDir();
         Assert.assertTrue( sftpWrapper.getLocalActualDir().contains(  "java" ) );
         Assert.assertTrue( sftpWrapper.getRemoteActualDir().contains( "java" ) );

         try( ByteArrayInputStream is = new ByteArrayInputStream( meinText.getBytes( charEncoding ) ) ) {
            sftpWrapper.createRemoteFile( is, remoteFilePathA );
         }
         sftpWrapper.downloadFile( remoteFilePathA, localFilePathB );
         sftpWrapper.uploadFile(   localFilePathB, remoteFilePathC );

         boolean a = false, c = false;
         List<FileData> fds = sftpWrapper.getFileDataList( remoteSftpSubdir );
         for( FileData fd : fds ) {
            if( "a.txt".equals( fd.name ) ) { a = true; }
            if( "c.txt".equals( fd.name ) ) { c = true; }
         }
         Assert.assertTrue( a && c );
         Assert.assertTrue( fds.size() >= 2 );
      }

      SFtpWrapper.downloadAndUnzip( remoteSftpSubdir, "target/sftp-local-dl", ".txt", "7", BENUTZERNAME, PASSWORT, HOST, "" + PORT );
   }
}
