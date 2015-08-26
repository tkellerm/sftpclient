package test.de.abasgmbh.utils.sftp;

import java.io.IOException;
import java.util.Arrays;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;

public class SFtpTestUtil
{
   public static SshServer startSFtpServer( final String benutzername, final String passwort, int port ) throws IOException
   {
      SshServer sftpServer = SshServer.setUpDefaultServer();
      sftpServer.setPort( port );
      sftpServer.setKeyPairProvider( new SimpleGeneratorHostKeyProvider( "target/hostkey.ser" ) );
      sftpServer.setSubsystemFactories( Arrays.<NamedFactory<Command>> asList( new SftpSubsystem.Factory() ) );
      sftpServer.setPasswordAuthenticator( new PasswordAuthenticator() {
         @Override
         public boolean authenticate( String username, String password, ServerSession session ) {
            return benutzername.equals( username ) && passwort.equals( password );
         }
      } );
      sftpServer.start();
      return sftpServer;
   }
}