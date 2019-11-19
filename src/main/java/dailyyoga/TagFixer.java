package dailyyoga;

import org.blinkenlights.jid3.ID3Exception;
import org.blinkenlights.jid3.ID3Tag;
import org.blinkenlights.jid3.MP3File;
import org.blinkenlights.jid3.MediaFile;
import org.blinkenlights.jid3.v1.ID3V1_0Tag;
import org.blinkenlights.jid3.v2.ID3V2_3_0Tag;


import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.io.*;

/**
 * Created by almatarm on 09/11/2019.
 */
public class TagFixer
{
    public static void main(String[] args) throws ID3Exception {
        // the file we are going to read
        File oSourceFile = new File("/Users/almatarm/tmp/OpenAudible/output/The Tower of the Swallow A Witcher Novel 001.mp3");

//        // create an MP3File object representing our chosen file
//        MediaFile oMediaFile = new MP3File(oSourceFile);
//
//
//        // any tags read from the file are returned, in an array, in an order which you should not assume
//        ID3Tag[] aoID3Tag = oMediaFile.getTags();
//        // let's loop through and see what we've got
//        // (NOTE:  we could also use getID3V1Tag() or getID3V2Tag() methods, if we specifically want one or the other)
//        for (int i=0; i < aoID3Tag.length; i++)
//        {
//            // check to see if we read a v1.0 tag, or a v2.3.0 tag (just for example..)
//            if (aoID3Tag[i] instanceof ID3V1_0Tag)
//            {
//                ID3V1_0Tag oID3V1_0Tag = (ID3V1_0Tag)aoID3Tag[i];
//                // does this tag happen to contain a title?
//                if (oID3V1_0Tag.getTitle() != null)
//                {
//                    System.out.println("Title = " + oID3V1_0Tag.getTitle());
//                }
//                // etc.
//            }
//            else if (aoID3Tag[i] instanceof ID3V2_3_0Tag)
//            {
//                ID3V2_3_0Tag oID3V2_3_0Tag = (ID3V2_3_0Tag)aoID3Tag[i];
//                // check if this v2.3.0 frame contains a title, using the actual frame name
//                if (oID3V2_3_0Tag.getTIT2TextInformationFrame() != null)
//                {
//                    System.out.println("Title = " + oID3V2_3_0Tag.getTIT2TextInformationFrame().getTitle());
//                }
//                // but check using the convenience method if it has a year set (either way works)
//                try
//                {
//                    System.out.println("Year = " + oID3V2_3_0Tag.getYear());  // reads TYER frame
//                }
//                catch (ID3Exception e)
//                {
//                    // error getting year.. if one wasn't set
//                    System.out.println("Could get read year from tag: " + e.toString());
//                }
//                // etc.
//            }
//        }

        try {

            InputStream input = new FileInputStream(oSourceFile);
            ContentHandler handler = new DefaultHandler();
            Metadata metadata = new Metadata();
            Parser parser = new Mp3Parser();
            ParseContext parseCtx = new ParseContext();
            parser.parse(input, handler, metadata, parseCtx);
            input.close();

            // List all metadata
            String[] metadataNames = metadata.names();

            for(String name : metadataNames){
                System.out.println(name + ": " + metadata.get(name));
            }

            // Retrieve the necessary info from metadata
            // Names - title, xmpDM:artist etc. - mentioned below may differ based
            System.out.println("----------------------------------------------");
            System.out.println("Title: " + metadata.get("title"));
            System.out.println("Artists: " + metadata.get("xmpDM:artist"));
            System.out.println("Composer : "+metadata.get("xmpDM:composer"));
            System.out.println("Genre : "+metadata.get("xmpDM:genre"));
            System.out.println("Album : "+metadata.get("xmpDM:album"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TikaException e) {
            e.printStackTrace();
        }
    }
}
