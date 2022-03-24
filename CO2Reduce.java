package org.App;

import java.util.Arrays;
import java.util.Iterator;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.GenericOptionsParser;

public class CO2Reduce extends Reducer<Text, Text, Text, Text> {

	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
		
		String malus_bonus;
		String rejet;
		String cout;
        String line;
		String prix;

		int add_prix = 0;
		int add_rejet = 0;
		int add_cout = 0;
		int moy_prix = 0;
		int moy_rejet = 0;
		int moy_cout = 0;
        int compteur=0;

        String [] line_split;

		Iterator<Text> i = values.iterator();
		while(i.hasNext()) {

		    line= i.next().toString(); 

			System.err.print(key);
			System.err.print("	");
			//System.err.println(node);

			line_split= line.split(","); 
			prix = line_split[0];
			rejet = line_split[1];
			cout = line_split[2];

			add_prix += Integer.parseInt(prix);
			add_rejet += Integer.parseInt(rejet);
			add_cout += Integer.parseInt(cout);

			compteur+=1;
		}
		moy_prix = add_prix/compteur;
		moy_rejet = add_rejet/compteur;
		moy_cout = add_cout/compteur;

		context.write(key, new Text( "Prix : "+moy_prix + "\t Rejet : " + moy_rejet + "\t Cout : " + moy_cout));
	}
}