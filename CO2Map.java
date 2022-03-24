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
	
public class CO2Map extends Mapper<Object, Text, Text, Text> {
	
		
	protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        int skip=0;
        int int_prix;
        int int_cout;
        int int_rejet;

        String marque;
        String strprix;
        String strcout;
        String strrejet;
        String line = value.toString();

        String marque_modele [];
        String modele_char [];
        String prix [];
        String cout [];
        
        
        String[] line_split = line.split(",");
        marque_modele = line_split[1].split(" ");
        modele_char=marque_modele[0].split("");
        prix = line_split[2].split("€");
        cout = line_split[4].split("€");

        //On enleve la 1ere colonne
        if (skip==0){
            skip=1;
            return;
        }

		line = line.replaceAll("\\u00a0"," ");
		
		 

         //On rentre la marque 
        
         if (modele_char[0] == "\""){
            marque=marque_modele[0].substring(1)+",";
            
        }
        else{
            marque=marque_modele[0];
        }

      

		// On rentre le prix du Bonus Malus
        strprix=prix[0];
		strprix = strprix.replaceAll(" ", "");

		if (strprix=="150kW(204ch)" || strprix=="100kW(136ch)"||strprix=="")
		{
			return;
		}

         // On rentre Rejet CO2
        strrejet=line_split[3];

		//On rentre le cout 
 	    strcout=cout[0];
        strcout=strcout.replaceAll(" ", "");
		

		

		int_prix = Integer.parseInt(strprix);
		int_rejet = Integer.parseInt(strrejet);
		int_cout = Integer.parseInt(strcout);
	
		// couple clé/valeurs
		String new_value = String.valueOf(int_prix) + "," +  String.valueOf(int_rejet) + "," + String.valueOf(int_cout);
		
        context.write(new Text(marque), new Text(new_value));
	}
}