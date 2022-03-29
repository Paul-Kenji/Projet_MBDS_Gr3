package airbase;

import oracle.kv.KVStore;
import java.util.List;
import java.util.Iterator;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.FaultException;
import oracle.kv.StatementResult;
import oracle.kv.table.TableAPI;
import oracle.kv.table.Table;
import oracle.kv.table.Row;
import oracle.kv.table.PrimaryKey;
import oracle.kv.ConsistencyException;
import oracle.kv.RequestTimeoutException;
import java.lang.Integer;
import oracle.kv.table.TableIterator;
import oracle.kv.table.EnumValue;
import java.io.File;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.List;



/**
 * Cette classe fournit les fonctions nécessaires pour gérer les tables.
 * La fonction void executeDDL(String statement) reçoit en paramètre
 * une commande ddl et l'applique dans la base nosql.
 * La displayResult affiche l'état de l'exécution de la commande
 * la fonction createTableCritere permet de créer une table critère>.
 */


 public class Airbase {
    private final KVStore store;
	private final String myTpPath="/home/BOCHATON/Projet";
//	private final String myTpPath="/home/myLinuxLogin/TpBigData";

	private final String tabClients="CATALOGUE_ESTIA2022_PK";

//	private final String tabCriteres="CRITERES_myMaster_myLinuxLogin";
//	private final String tabClients="CLIENTS_myMaster_myLinuxLogin";
//	private final String tabAppreciations="APPRECIATIONS_myMaster_myLinuxLogin";

    /**
     * Runs the DDL command line program.
     */
    public static void main(String args[]) {
        try {
            	Airbase arb= new Airbase(args);
		          arb.initAirbaseTablesAndData(arb);

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
    /**
     * Parses command line args and opens the KVStore.
     */
	Airbase(String[] argv) {

		String storeName = "kvstore";
		String hostName = "localhost";
		String hostPort = "5000";

		final int nArgs = argv.length;
		int argc = 0;
		store = KVStoreFactory.getStore
		    (new KVStoreConfig(storeName, hostName + ":" + hostPort));
	}


/**
* Affichage du résultat pour les commandes DDL (CREATE, ALTER, DROP)
*/

	private void displayResult(StatementResult result, String statement) {
		System.out.println("===========================");
		if (result.isSuccessful()) {
			System.out.println("Statement was successful:\n\t" +
			statement);
			System.out.println("Results:\n\t" + result.getInfo());
		} else if (result.isCancelled()) {
			System.out.println("Statement was cancelled:\n\t" +
			statement);
		} else {
			/*
			* statement was not successful: may be in error, or may still
			* be in progress.
			*/
			if (result.isDone()) {
				System.out.println("Statement failed:\n\t" + statement);
				System.out.println("Problem:\n\t" +
				result.getErrorMessage());
			}
			else {

				System.out.println("Statement in progress:\n\t" +
				statement);
				System.out.println("Status:\n\t" + result.getInfo());
			}
		}
	}

	/*
		La méthode initAirbaseTablesAndData permet :
		- de supprimer les tables si elles existent
		- de créer des tables
		- Insérer des critères
		- et charger les clients et les appréciations
	**/

	public void initAirbaseTablesAndData(Airbase arb) {
		arb.dropTableClient();
		arb.createTableClient();
		arb.loadClientDataFromFile(myTpPath+"/airbase/Catalogue.csv");
	}



	/**
		public void dropTableClient()
		M&thode de suppression de la table client.
	*/
	public void dropTableClient() {
		String statement = null;

		statement ="drop table "+tabClients;
		executeDDL(statement);
	}




	/**
		public void createTableClient()
		M&thode de création de la table client.
	*/

	public void createTableClient() {
		String statement = null;
		statement="create table "+tabClients+" ("
		+"ClIENTID INTEGER,"
    +"MARQUE STRING,"
		+"NOM STRING,"
		+"PUISSANCE STRING," //CODEPOSTAL
		+"LONGUEUR STRING," //VILLE
		+"NBPLACES STRING," //ADRESSE
		+"NBPORTES STRING," //TELEPHONE
		+"COULEUR STRING," //couleur
    +"OCCASION STRING," //
    +"PRIX STRING," //
		+"PRIMARY KEY(ClIENTID))";
		executeDDL(statement);

	}

	/**
		public void executeDDL(String statement)
		méthode générique pour executer les commandes DDL
	*/
	public void executeDDL(String statement) {
		TableAPI tableAPI = store.getTableAPI();
		StatementResult result = null;

		System.out.println("****** Dans : executeDDL ********" );
		try {
		/*
		* Add a table to the database.
		* Execute this statement asynchronously.
		*/

		result = store.executeSync(statement);
		displayResult(result, statement);
		} catch (IllegalArgumentException e) {
		System.out.println("Invalid statement:\n" + e.getMessage());
		} catch (FaultException e) {
		System.out.println("Statement couldn't be executed, please retry: " + e);
		}
	}

	/**
		private void insertAClientRow(int clientid, String nom, String prenom, String codePostal, String ville,
			String adresse, String telephone, String couleur)
		Cette méthode insère une nouvelle ligne dans la table CLIENT
	*/

	private void insertAClientRow(int clientid, String marque, String nom, String puissance, String longueur,
			String nbPlaces, String nbPortes, String couleur, String occasion, String prix){
		//TableAPI tableAPI = store.getTableAPI();
		StatementResult result = null;
		String statement = null;
		System.out.println("********************************** Dans : insertAClientRow *********************************" );

		try {

			TableAPI tableH = store.getTableAPI();
			// The name you give to getTable() must be identical
			// to the name that you gave the table when you created
			// the table using the CREATE TABLE DDL statement.
			Table tableClient = tableH.getTable(tabClients);

			// Get a Row instance
			Row clientRow = tableClient.createRow();
			// Now put all of the cells in the row.
			// This does NOT actually write the data to
			// the store.

			// Create one row
			clientRow.put("clientid", clientid);
      clientRow.put("marque", marque);
			clientRow.put("nom", nom);
			clientRow.put("puissance", puissance);
			clientRow.put("longueur", longueur);
			clientRow.put("nbPlaces", nbPlaces);
			clientRow.put("nbPortes", nbPortes);
			clientRow.put("couleur", couleur);
      clientRow.put("occasion", occasion);
      clientRow.put("prix", prix);

			// Now write the table to the store.
			// "item" is the row's primary key. If we had not set that value,
			// this operation will throw an IllegalArgumentException.
			tableH.put(clientRow, null, null);

		}
		catch (IllegalArgumentException e) {
			System.out.println("Invalid statement:\n" + e.getMessage());
		}
		catch (FaultException e) {
			System.out.println("Statement couldn't be executed, please retry: " + e);
		}

	}


	/**
		void loadClientDataFromFile(String clientDataFileName)
		cette methodes permet de charger les clients depuis le fichier
		appelé clientData_txt.txt.
		Pour chaque client chargé, la
		méthide insertAClientRow sera appélée
	*/
	void loadClientDataFromFile(String clientDataFileName){
		InputStreamReader 	ipsr;
		BufferedReader 		br=null;
		InputStream 		ips;

		// Variables pour stocker les données lues d'un fichier.
		String 		ligne;
		System.out.println("********************************** Dans : loadClientDataFromFile *********************************" );

		/* parcourir les lignes du fichier texte et découper chaque ligne */
		try {
			ips  = new FileInputStream(clientDataFileName);
			ipsr = new InputStreamReader(ips);
			br   = new BufferedReader(ipsr);

      int i = 0;
			/* open text file to read data */

			//parcourir le fichier ligne par ligne et découper chaque ligne en
			//morceau séparés par le symbole ;
			while ((ligne = br.readLine()) != null) {
				//int clientid;
				//String nom, prenom, codePostal, ville, adresse, telephone, couleur;

				ArrayList<String> clientRecord= new ArrayList<String>();
				StringTokenizer val = new StringTokenizer(ligne,",");
				while(val.hasMoreTokens()) {
						clientRecord.add(val.nextToken().toString());
				}
				int clientid		= i;
        String marque		= clientRecord.get(0);
				String nom		= clientRecord.get(1);
				String puissance = clientRecord.get(2);
				String longueur	= clientRecord.get(3);
				String nbPlaces = clientRecord.get(4);
				String nbPortes	= clientRecord.get(5);
				String couleur	= clientRecord.get(6);
        String occasion	= clientRecord.get(7);
        String prix	= clientRecord.get(8);
				System.out.println(" clientid="+clientid+" marque="+marque+" nom="+nom
				+" puissance="+puissance+" longueur="+longueur+" nbPlaces="+nbPlaces
				+" nbPortes="+nbPortes+" couleur="+couleur +" occasion="+occasion + " prix"+prix);
				// Add the client in the KVStore
				insertAClientRow(clientid, marque, nom, puissance, longueur, nbPlaces, nbPortes, couleur, occasion, prix);
        i++;
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}


	/**
		private void displayClientRow (Row clientRow)
		Cette méthode d'afficher une ligne de la table client.
	*/
	private void displayClientRow (Row clientRow) {
   		System.out.println("========== DANS : displayClientRow =================");
  		//System.out.println("===========================");
		Integer clientid = 	clientRow.get("clientid").asInteger().get();
    String marque = clientRow.get("marque").asString().get();
		String nom = clientRow.get("nom").asString().get();
		String puissance = clientRow.get("puissance").asString().get();
		String longueur = clientRow.get("longueur").asString().get();
		String nbPlaces = clientRow.get("nbPlaces").asString().get();
		String nbPortes = clientRow.get("nbPortes").asString().get();
		String couleur = clientRow.get("couleur").asString().get();
    String occasion = clientRow.get("occasion").asString().get();
    String prix = clientRow.get("prix").asString().get();

		System.out.println(" Client row :{"+" clientid="+clientid+
		  " marque="+marque+" nom="+nom +" puissance="+puissance+" longueur="+longueur+" nbPlaces="+nbPlaces
		  +" nbPortes="+nbPortes+" couleur="+couleur+" occasion="+occasion+" prix="+prix+"}");
	}

	/**
		private void getClientByKey (int clientId)
		Cette méthode de charger  une ligne de la table client
		connaissant sa clé
	*/
	public void getClientByKey(int clientId){

		StatementResult result = null;
		String statement = null;
		System.out.println("\n\n********************************** Dans : getClientByKey *********************************" );

		try {
			TableAPI tableH = store.getTableAPI();
			// The name you give to getTable() must be identical
			// to the name that you gave the table when you created
			// the table using the CREATE TABLE DDL statement.
			Table tableClient = tableH.getTable("client");

			PrimaryKey key=tableClient.createPrimaryKey();
			key.put("clientId", clientId);
			// Retrieve the row. This performs a store read operation.
			// Exception handling is skipped for this trivial example.
			Row row = tableH.get(key, null);
			// Now retrieve the individual fields from the row.
			displayClientRow(row);
		} catch (IllegalArgumentException e) {
			System.out.println("Invalid statement:\n" + e.getMessage());
		} catch (FaultException e) {
			System.out.println("Statement couldn't be executed, please retry: " + e);
		}


	}

	/**
		public void getClientRows()
		Cette méthode permet de charger  toutes les lignes de la table client
		connaissant sa clé
	*/
	public void getClientRows(){

		TableAPI tableAPI = store.getTableAPI();
		StatementResult result = null;
		String statement = null;
		System.out.println("******************************** LISTING DES CLIENTS ******************************************* ");

		try {
			TableAPI tableH = store.getTableAPI();
			// The name you give to getTable() must be identical
			// to the name that you gave the table when you created
			// the table using the CREATE TABLE DDL statement.
			Table tableClient = tableH.getTable("client");

			PrimaryKey key = tableClient.createPrimaryKey();
			//key.put("critereId", 1);
			//key.put("adr", adr);

			// Exception handling is omitted, but in production code
			// ConsistencyException, RequestTimeException, and FaultException
			// would have to be handle
			TableIterator<Row> iter = tableH.tableIterator(key, null, null);
			try {
				while (iter.hasNext()) {
					Row clientRow = iter.next();
					displayClientRow(clientRow);
				}
			} finally {
				if (iter != null) {
				iter.close();
			}
			}
		}
		catch (IllegalArgumentException e) {
			System.out.println("Invalid statement:\n" + e.getMessage());
		}
		catch (FaultException e) {
			System.out.println("Statement couldn't be executed, please retry: " + e);
		}
	}
 }
