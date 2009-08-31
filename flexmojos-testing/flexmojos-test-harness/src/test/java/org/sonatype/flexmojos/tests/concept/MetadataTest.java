package org.sonatype.flexmojos.tests.concept;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.it.Verifier;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import flash.swf.tools.SwfxPrinter;

public class MetadataTest
    extends AbstractConceptTest
{

    @Test
    public void testMetadataTest()
        throws Exception
    {
        Verifier v = standardConceptTester( "metadata-test" );
        File testDir = new File( v.getBasedir() );
        final String swfPath = new File( testDir, "target/metadata-test-1.0-SNAPSHOT.swf" ).getAbsolutePath();
        final String swfDumpPath = swfPath + "x";
        SwfxPrinter.main( new String[] { "-out", swfDumpPath, swfPath } );

        String dump = FileUtils.fileRead( swfDumpPath );
        dump = dump.substring( dump.indexOf( "<Metadata>" ), dump.indexOf( "</Metadata>" ) + 11 );

        Xpp3Dom dom;
        try
        {
            dom = Xpp3DomBuilder.build( new StringReader( dump ) );
        }
        catch ( Exception e )
        {
            Assert.fail( "Unable to parse \n" + dump, e );
            throw new RuntimeException( e ); // wont happen
        }
        Xpp3Dom metadata = dom.getChild( "rdf:RDF" ).getChild( "rdf:Description" );
        Assert.assertNotNull( metadata );

        String description = metadata.getChild( "dc:description" ).getValue();
        Assert.assertEquals( description, "Some kind of description text for test flex-metadata bugs" );

        Xpp3Dom title = metadata.getChild( "dc:title" ).getChild( "rdf:Alt" ).getChild( "rdf:li" );
        Assert.assertEquals( title.getValue(), "title for en-us locale from metadata" );
        Assert.assertEquals( title.getAttribute( "xml:lang" ), "en-us" );

        Xpp3Dom[] creators = metadata.getChildren( "dc:creator" );
        final int developersCount = 3;
        Assert.assertEquals( creators.length, developersCount );
        // order not saved, random
        List<String> creatorsNames = new ArrayList<String>( developersCount );
        for ( int i = 0; i < developersCount; i++ )
        {
            creatorsNames.add( creators[i].getValue() );
        }
        Assert.assertTrue( creatorsNames.contains( "Marvin Herman Froeder" ) );
        Assert.assertTrue( creatorsNames.contains( "Joost den Boer" ) );
        Assert.assertTrue( creatorsNames.contains( "Logan Allred" ) );

        Assert.assertEquals( metadata.getChild( "dc:contributor" ).getValue(), "Justin" );

        Assert.assertEquals( metadata.getChild( "dc:language" ).getValue(), "en_US" );
    }

}
