package org.example.service;

import org.example.model.SongMetadata;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;

@Service
public class MetadataExtractorService {

    public SongMetadata extractMetadata(File file) throws Exception {
        Metadata metadata = new Metadata();
        ContentHandler handler = new DefaultHandler();
        ParseContext parseContext = new ParseContext();

        try (FileInputStream input = new FileInputStream(file)) {
            Mp3Parser parser = new Mp3Parser();
            parser.parse(input, handler, metadata, parseContext);
        }

        SongMetadata songMetadata = new SongMetadata();
        songMetadata.setTitle(metadata.get("title"));
        songMetadata.setArtist(metadata.get("xmpDM:artist"));
        songMetadata.setAlbum(metadata.get("xmpDM:album"));
        songMetadata.setDuration(metadata.get("xmpDM:duration"));
        songMetadata.setContentType(metadata.get("Content-Type"));

        return songMetadata;
    }
}

