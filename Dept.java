import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DB;
import org.bson.Document;
import java.util.Arrays;
import java.util.List;
import com.mongodb.client.FindIterable;
import java.util.Iterator;
import java.util.ArrayList;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.model.UpdateOptions;

public class Dept {
   private MongoDatabase database;
   private String dbName="BOCHATON_DERIAN_ROUSSY_HOUMMANI";
   private String hostName="localhost";
   private int port=27017;
   private String userName="urh";
   private String passWord="passUrh";
   private String CollectionName="IMMATRICULATION";


  public static void main( String args[] ) {
      try{
  		Dept dept = new Dept();

  		dept.dropCollection(dept.CollectionName);
  		dept.createCollection(dept.CollectionName);
  		dept.importFile(dept.CollectionName);

      }catch(Exception e){
  		    e.printStackTrace();
  	  }
  }
   /**
	Constructeur Dept.
	Dans ce constructeur sont effectuées les activités suivantes:
	- Création d'une instance du client MongoClient
	- Création d'une BD Mongo appelé BOCHATON_DERIAN_ROUSSY_HOUMMANI
	- Création d'un utilisateur appelé
	- Chargement du pointeur vers la base BOCHATON_DERIAN_ROUSSY_HOUMMANI
   */
   Dept(){
		// FD1 : Creating a Mongo client
		MongoClient mongoClient = new MongoClient( hostName , port );

		// Creating Credentials
		MongoCredential credential;
		credential = MongoCredential.createCredential(userName, dbName,
		 passWord.toCharArray());
		System.out.println("Connected to the database successfully");
		System.out.println("Credentials ::"+ credential);
		// Accessing the database
		database = mongoClient.getDatabase(dbName);
   }

   /**
	FD2 : Cette fonction permet de créer une collection
	de nom nomCollection.
   */
   public void createCollection(String nomCollection){
		//Creating a collection
		database.createCollection(nomCollection);
		System.out.println("Collection IMMATRICULATION created successfully");
   }

   /**
	FD3 : Cette fonction permet de supprimer une collection
	de nom nomCollection.
   */
   public void dropCollection(String nomCollection){
		//Drop a collection
		MongoCollection<Document> IMMATRICULATION=null;
		System.out.println("\n\n\n*********** dans dropCollectionIMMATRICULATION *****************");

		System.out.println("!!!! Collection : "+IMMATRICULATION);

		IMMATRICULATION=database.getCollection(nomCollection);
		System.out.println("!!!! Collection Dept : "+IMMATRICULATION);
		// Visiblement jamais !!!
		if (IMMATRICULATION==null)
			System.out.println("Collection inexistante");
		else {
			IMMATRICULATION.drop();
			System.out.println("Collection IMMATRICULATION removed successfully !!!");
		}
   }

   /**
	FD4 : Cette fonction permet d'insérer un Departement dans une collection.
   */

   public void importFile(){
     Runtime r = Runtime.getRuntime();
     Process p = null;
     String command = "mongoimport --db users --collection IMMATRICULATION --type csv --file /opt/backups/contacts.csv";
     try {
       p = r.exec(command);
       System.out.println("Reading csv into Database");
       
     } catch (Exception e){
       System.out.println("Error executing " + command + e.toString());
     }
   }

}
