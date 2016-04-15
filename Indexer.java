package com.ck.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Indexer {

	private IndexWriter writer;
	
	public Indexer(String indexDirectoryPath) throws IOException{
		 Directory indexDirectory = 
		         FSDirectory.open(new File(indexDirectoryPath).toPath());
		 //create the indexer
		 IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		 config.setOpenMode(OpenMode.CREATE);
	     writer = new IndexWriter(indexDirectory, config);
	}

	 public void close() throws CorruptIndexException, IOException{
	      writer.close();
	   }
	 
	 private Document getDocument(Path inputFile) throws IOException{
		 
		 FieldType contentFieldType = new FieldType();
		 contentFieldType.setStored(true);
		 contentFieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
		 InputStream stream = Files.newInputStream(inputFile);
		 Field content = new TextField("content", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)));
		 
		 FieldType nameFieldType = new FieldType();
		 nameFieldType.setStored(true);
		 nameFieldType.setIndexOptions(IndexOptions.DOCS);
		 Field name = new StringField("name", inputFile.getFileName().toString(),Field.Store.YES); 
		 Field path = new StringField("path",inputFile.toString(),Field.Store.YES);
		 
		 Document doc = new Document();
		 doc.add(name);
		 doc.add(path);
		 doc.add(content);
		 return doc;
	 }
	 
	 public void commit() throws IOException{
		 writer.close();		 
	 }
	 
	 public void indexDocument(Path inputFile) throws IOException{
		 Document doc = getDocument(inputFile);
		 String path = doc.get("path");
		 writer.addDocument(doc);
	 }
	 
	 public static void main(String args[]){
//		 C:\APPS\lucene\index
		 String indexDirectoryPath = "C:\\APPS\\lucene\\index";
		 Indexer indexer = null;
		 try {
			indexer = new Indexer(indexDirectoryPath);
			final Set<Path> filesToIndex = new HashSet<Path>();
				 Path path = Paths.get("C:\\APPS\\lucene\\data");
				 if(Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)){
					 Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
						 @Override
						public FileVisitResult visitFile(Path file,
								BasicFileAttributes attrs) throws IOException {
							 System.out.println("Found file :"+file);
							 if(file.getFileName().toString().endsWith("txt") || file.getFileName().toString().endsWith("log")){
								 System.out.println("Adding file :"+file);
								 filesToIndex.add(file);
							 }
							return FileVisitResult.CONTINUE; 							 
						}
					});
				 }
				 for (Path file : filesToIndex) {
					 System.out.println("Indexing file:"+file);
					 indexer.indexDocument(file);
				}
				 indexer.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		}
		 
	 }
}
