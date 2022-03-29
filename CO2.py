###Il faut transformer la partie marqueModèle en clé (la nettoyer avant donc) pour pouvoir merge avec catalogue
###le map doit donc: - Supprimer la première colonne / deja fait dans le premier map l7
###                  - Nettoyer la futur Key
###                  - Nettoyer la valeur
###                  - rendre la futur clé "fusionnable" avec la futur clé de catalogue

###Le catalogue doit : - Avoir une clé étant Marque + Modèle => le map s'en chargera


###Le réduce doit: - Merge CO2 dans Catalogue
###                - Calculer la moyenne de chaque valeur numérique
###                - Insérer cette moyenne si la valeur n'existe pas dans CO2

### On s'occupe d'importer et de rendre les données de CO2 le plus propre possible avant de travailler sur les deux RDD en meme temps
CO2 = sc.textFile('CO2.csv') #Import des données 
CO2_clear= CO2.mapPartitionsWithIndex(lambda id_x, iter: list(iter)[1:] if(id_x == 0) else iter) #Suppression du header
CO2_clear.collect() #Aperçu du résultat + calcul
CO2_rdy = CO2_clear.map(lambda x: (x.split(",")[1], x.split(",")[2:])) #transformation en couple cle/valeur du type marque/(reste des données)
###value est un couple cle/valeur
def MapCO2(value): 
    key = value[0]
    valeur = value[1]
    if valeur[0] == ' 150kW (204ch)"' or valeur[0] == ' 100kW (136ch)"':
        valeur.pop(0)
    key = key.replace('"', '', 1) #On enleve les double cotes au début de certaine ligne
    ###On recupere les marques pour en faire la cle de notre rdd
    ###LAND ROVER etant la seuls marque de notre fichier à etre en deux mots on s'assure que le nom complet soit la cle 
    if (key.split(' ')[0] == 'LAND'):
        marque = key.split(' ')[0] + ' ' + key.split(' ')[1]
    else:
        marque = key.split(' ')[0]
    ###On attribut nos valeurs et on remplace ce qui dérange dans le format des nombres que l'on devra traiter.
    bonusMalus = valeur[0]
    rejet = valeur[1]
    cout = valeur[2]
    bonusMalus = bonusMalus.replace('€ 1', '')
    bonusMalus = bonusMalus.replace('€', '')
    bonusMalus = bonusMalus.replace('\xa0', '')
    ###On crée des compteurs pour chaque valeur, qui nous serviront pour calculer les moyennes
    ###On les met a 0 lorsque la valeur n'est pas attribuée dans CO2.csv pour eviter de fausser les moyennes
    cptBM = 1
    cptRejet =1 
    cptCout = 1
    if bonusMalus == '-':
        bonusMalus = 0
        cptBM = 0
    rejet = rejet.replace('€ 1', '')
    rejet = rejet.replace('€', '')
    rejet = rejet.replace('\xa0', '')
    if rejet == '-':
        rejet = 0
        cptRejet = 0
    cout = cout.replace('€ 1', '')
    cout = cout.replace('€', '')
    cout = cout.replace('\xa0', '')
    if cout == '-':
        cout = 0
        cptCout = 0
    listVal = (int(bonusMalus), int(rejet), int(cout), cptBM, cptRejet, cptCout)
    ###On renvoi soit le format Key: String, Value: List
    ###La liste de valeur contient les trois valeurs qui nous interesse et les compteur qui leur sont associés
    return  marque, listVal;

### Fonction pour calculer les sommes pour chaque marque
def CO2Red(x, y):
    listVal = []
    for i in range(len(x)):
        listVal.append(x[i] + y[i])
    return listVal;

CO2_mapped = CO2_rdy.map(lambda x: MapCO2(x)) #On utilise un map "normal" pour travailler sur la cle ET la valeur
CO2_mapped.collect()

###On cherche à obtenir un RDD qui contient des couples Marque / la somme de chacune des 6 valeurs de notre liste
CO2_reduced = CO2_mapped.reduceByKey(lambda x, y: CO2Red(x, y))  

###Fonction pour calculer les trois moyennes de chaque marque
def CO2Avg(listeVal):
    listRes = []
    if listeVal[3] == 0:
        listRes.append('N/A')
    else:
        listRes.append(listeVal[0]/listeVal[3])
    if listeVal[4] == 0:
        listRes.append('N/A')
    else:
        listRes.append(listeVal[1]/listeVal[4])
    if listeVal[5] == 0:
        listRes.append('N/A')
    else:
        listRes.append(listeVal[2]/listeVal[5])
    return listRes;

CO2_avg = CO2_reduced.mapValues(lambda x: CO2Avg(x))
CO2_avg.collect()
###On change la casse pour pouvoir faire se rejoindre les deux fichiers CO2 et Catalogue
CO2_avg_lower = CO2_avg.map(lambda x: (x[0].lower(), x[1]))
CO2_avg_lower.collect()

###On recupère chaque moyenne globale
###Pour la valeur Bonus/Malus
avgBMRDD = CO2_mapped.map(lambda x: ('bonusMalus', [x[1][0], x[1][3]]))
avgBM2 = avgBMRDD.reduceByKey(lambda x, y: (x[0]+y[0], x[1]+y[1])).map(lambda x: x[1][0]/x[1][1])
avgBM = avgBM2.take(1)[0][0]/avgBM2.take(1)[0][1]

###Pour la valeur Rejet
avgRejetRDD = CO2_mapped.map(lambda x: ('rejet', [x[1][1], x[1][4]]))
avgRejet2 = avgRejetRDD.reduceByKey(lambda x, y: (x[0]+y[0], x[1]+y[1])).map(lambda x: x[1])
avgRejet = avgRejet2.take(1)[0][0]/avgRejet2.take(1)[0][1]

###Pour la valeur Cout Energie
avgCoutRDD = CO2_mapped.map(lambda x: ('cout', [x[1][2], x[1][5]]))
avgCout2 = avgCoutRDD.reduceByKey(lambda x, y: (x[0]+y[0], x[1]+y[1])).map(lambda x: x[1])
avgCout = avgCout2.take(1)[0][0]/avgCout2.take(1)[0][1]

### On s'occupe d'importer et de rendre les données de Catalogue le plus propre possible avant de travailler sur les deux RDD en meme temps
catalogue = sc.textFile('Catalogue.csv') #Import
catalogue2 = catalogue.mapPartitionsWithIndex(lambda id_x, iter: list(iter)[1:] if(id_x == 0) else iter) #Suppression du header
catalogue2.collect() #Calcul
cata_rdy = catalogue2.map(lambda x: (x.split(",")[0].lower(), x.split(",")[1:])) #transformation en couple cle/valeur du type marque/(reste des données)
cata_rdy2 = cata_rdy.mapValues(lambda x: x+[avgBM, avgRejet, avgCout]).collect() ###On rajoute les moyennes globales dans chaque ligne

cata_join_CO2 = cata_rdy2.leftOuterJoin(CO2_avg_lower)

###On nettoie le dernier fichier avec un Map
def cataloguefinal(x):
    if x[1] != None:
        if x[1][0] != 'N/A':
            x[0][8] = x[1][0]
        if x[1][1] != 'N/A':
            x[0][9] = x[1][1]
        if x[1][2] != 'N/A':
            x[0][10] = x[1][2]
    return tuple(x[0]);

catalogueFinal = cata_join_CO2.mapValues(lambda x: cataloguefinal(x))
catalogueFinal.collect()

###On sauvegarde dans HDFS le fichier
catalogueFinal.saveAsTextFile('res')

###On le recupère depuis HDFS vers la machine virtuelle d'abord, puis sur le disque dur de l'ordinateur
### La ligne de commande est la suivante (sans '###'):
### hadoop fs -get res /home/dantoine