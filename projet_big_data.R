setwd("C:/Program Files/RStudio")
#----------------------------------------------#
# DOWNLOADING AND INSTALLING REQUIRED PACKAGES #
#----------------------------------------------#
# Install packages (will update them if already installed)
install.packages("cluster")
install.packages(ggplot2)
# Activate packages
library(cluster)
library(ggplot2)
library(dbscan)
library(gridExtra)
#--------------------------------#
# DOWNLOADING DATA FROM CSV FILE #
#--------------------------------#
catalogue <- read.csv("Catalogue.csv", header = TRUE, sep = ",", dec = ".", stringsAsFactors = T)
clients_1 <- read.csv("ClientsA.csv", header = TRUE, sep = ",", dec = ".", stringsAsFactors = T)
clients_9 <- read.csv("ClientsB.csv", header = TRUE, sep = ",", dec = ".", stringsAsFactors = T)
CO2 <- read.csv("CO2.csv", header = TRUE, sep = ",", dec = ".", stringsAsFactors = T)
immatriculations <- read.csv("Immatriculations.csv", header = TRUE, sep = ",", dec = ".", stringsAsFactors = T)
marketing <- read.csv("Marketing.csv", header = TRUE, sep = ",", dec = ".", stringsAsFactors = T)
# Concat of 2 tables by clients
clients <- rbind(clients_1, clients_9)
#---------------#
# DATA CLEANING #
#---------------#
# Formatting of file
clients[clients == " " | clients == "N/D" | clients == "?" | clients == "-1"] <- NA
clients[clients == "Homme" | clients == "Masculin"] <- "M"
clients[clients == "Femme" | clients == "Féminin"] <- "F"
clients[clients == "seul" | clients == "seule"] <- "seul(e)"
#========================
# PREPARATION DES DONNEES 
#========================
catalogue$nbPlaces <- as.ordered(catalogue$nbPlaces)
catalogue$nbPortes <- as.ordered(catalogue$nbPortes)
summary(catalogue)
# Suppression des variables inutiles pour l'identification des categories de vehicules par clustering
dataset <- subset(catalogue, select = -c(marque, nom, couleur, occasion))
summary(dataset)
#--------------------------------
# Clustering hierarchique agnes()
#--------------------------------
# Ã‰chantillonnage alÃ©atoire pour les temps de calcul
set.seed(50)                                    # Pour la reproductibilitÃ© d'une exÃ©cution sur l'autre
liste_lignes <- sample(nrow(dataset),270)     # Liste des numÃ©ros de lignes sÃ©lectionnÃ©es alÃ©atoirement
dataset <- dataset[liste_lignes,]               # SÃ©lection des lignes par la liste des numÃ©ros 

# Matrice de distance
dmatrix <- daisy(dataset)
# Fonction d'affichage des distributions des valeurs des variables pour chaque cluster (clustering en paramÃ¨tre)
analyse <- function(c) {
  p1 = qplot(as.factor(c), data=dataset, fill=longueur, xlab = "Cluster", ylab = NULL,)
  p2 = qplot(as.factor(c), data=dataset, fill=nbPlaces, xlab = "Cluster")
  p3 = qplot(as.factor(c), data=dataset, fill=nbPortes, xlab = "Cluster")
  p4 = qplot(as.factor(c), puissance, data=dataset, color=puissance, geom = "boxplot", xlab = "Cluster")
  p5 = qplot(as.factor(c), prix, data=dataset, geom = "boxplot", xlab = "Cluster")
  grid.arrange(p1, p2, p3, p4, p5, ncol=2, nrow = 3)
}      
# Calcul par la fonction agnes() de la structure du dendrogramme
agn <- agnes(dmatrix)
# Affichage du dendrogramme
plot(agn, which.plots = 2)
# Generation de 6 clusters 
agn6 <- cutree(agn, k=6)
table(agn6)
analyse(agn6)
#---------------------------
# Attribution des catégories
#---------------------------
immatriculations['categorie'] <- NA
immatriculations$categorie[immatriculations$longueur == "courte" & immatriculations$nbPortes == 3] <- "petit3"
immatriculations$categorie[immatriculations$longueur == "courte" & immatriculations$nbPortes == 5] <- "petit5"
immatriculations$categorie[immatriculations$longueur == "moyenne" & immatriculations$nbPortes == 5] <- "moyenne"
immatriculations$categorie[immatriculations$longueur == "longue" & immatriculations$nbPortes == 5] <- "longue5"
immatriculations$categorie[immatriculations$longueur == "longue" & immatriculations$nbPortes == 7] <- "longue7"
immatriculations$categorie[immatriculations$longueur == "très longue" & immatriculations$nbPortes == 5] <- "tresLongue"
#-------------------------------------------------
# fusion des dataframe clients et immatriculations
#-------------------------------------------------
# Fusion of 2 tables
fichier_clients_complet <- merge(clients, immatriculations, by = "immatriculation")
summary(fichier_clients_complet$situationFamiliale)
View(fichier_clients_complet)


#-------------------------------------------------
# arbres décisionnels*******************************************************************************************************************
#-------------------------------------------------
# Chargement des donnees
table(fichier_clients_complet$categorie)
#200000 lignes environ
View(fichier_clients_complet)

# Construction des ensembles d'apprentissage et de test  (2/3= 133333) et (1/3 restant) de 200000
categorie_EA <- fichier_clients_complet[1:133333,]
categorie_ET <- fichier_clients_complet[133334:200000,]

# Suppression de la variable immatriculation
categorie_EA <- subset(categorie_EA, select = -immatriculation) 

#Supression de colonnes non nécessaires
categorie_EA <- categorie_EA[ , - c(7:15)]
# Autre solution par reference au numero de colonne : produit_EA <- produit_EA[,-1]

# Affichages des ensembles et des distributions de valeurs des variables
View(categorie_EA)
View(categorie_ET)
summary(categorie_EA)
summary(categorie_ET)

categorie_EA$categorie <- as.factor(as.character(categorie_EA$categorie))
categorie_ET$categorie <- as.factor(as.character(categorie_ET$categorie))
categorie_EA$nbEnfantsAcharge <- as.factor(as.character(categorie_EA$nbEnfantsAcharge))
categorie_ET$nbEnfantsAcharge <- as.factor(as.character(categorie_ET$nbEnfantsAcharge))
categorie_EA$age <- as.numeric(as.character(categorie_EA$age))
categorie_ET$age <- as.numeric(as.character(categorie_ET$age))
categorie_EA$sexe <- as.factor(as.character(categorie_EA$sexe))
categorie_ET$sexe <- as.factor(as.character(categorie_ET$sexe))
categorie_EA$situationFamiliale <- as.factor(as.character(categorie_EA$situationFamiliale))
categorie_ET$situationFamiliale <- as.factor(as.character(categorie_ET$situationFamiliale))

categorie_EA$X2eme.voiture <- as.factor(as.character(categorie_EA$X2eme.voiture))
categorie_ET$X2eme.voiture <- as.factor(as.character(categorie_ET$X2eme.voiture))

summary(categorie_EA)
summary(categorie_ET)

#--------------------------------#
# APPRENTISSAGE DE L'ARBRE TREE #
#--------------------------------#


install.packages(rpart)
install.packages(C5.0)
install.packages(tree)
library(rpart)
library(C50)
library(tree)

shuffle_index <- sample(1:nrow(categorie_EA))

tree_tree <- tree(categorie ~ age + sexe + situationFamiliale + nbEnfantsAcharge + X2eme.voiture , categorie_EA)
pred.tree_tree <- predict(tree_tree, categorie_ET, type="class")
table(categorie_ET$categorie,pred.tree_tree)
plot(tree_tree)
text(tree_tree, pretty=0)




#--------------------------------#
# APPRENTISSAGE DE L'ARBRE RPART #
#--------------------------------#

# Installation et activation de la librairie requise
install.packages("rpart")
library(rpart)

#rpart ne construit en général pas l'arbre le plus complet possible, pour des raisons d'efficacité. 
#Il est rare en pratique qu'un arbre très profond qui ne réalise aucune erreur de classement sur les données d'apprentissage soit le plus adapté.

# Construction de l'arbre de decision 'tree_rpart'
tree_rpart <- rpart(categorie~  age + sexe + situationFamiliale + X2eme.voiture + nbEnfantsAcharge, categorie_EA)

# Affichage de l'arbre par 'tree_rpart' par plot.rpart() et text.rpart() 
plot(tree_rpart)
text(tree_rpart, pretty=0)

pred.tree_rpart <- predict(tree_rpart, categorie_ET, type="class")
table(categorie_ET$categorie,pred.tree_rpart)

# Affichage textuel de l'arbre de decision
print(tree_rpart)


#--------------------------------#
# APPRENTISSAGE DE L'ARBRE C5.0 #
#--------------------------------#

install.packages("C50")
library(C50)

# Apprentissage arbre
tree_c5 <- C5.0(categorie~ sexe + situationFamiliale + nbEnfantsAcharge +X2eme.voiture+age, categorie_EA[shuffle_index,])

pred.tree_c5 <- predict(tree_c5, categorie_ET, type="class")
table(categorie_ET$categorie,pred.tree_c5)

# Affichage graphique par plot.C5.0
plot(tree_c5, type="simple")


#meilleur algo donc test avec marketing
marketing$nbEnfantsAcharge <- as.factor(as.character(marketing$nbEnfantsAcharge))
pred.marketing <- predict(tree_c5, marketing, type="class")
#affichage en arbre
plot(pred.marketing , type="simple")
#---------------------------
# Attribution des catégories
#---------------------------
marketing['categorie'] <- NA
marketing$categorie[pred.marketing == "longue5"] <- "longue5"
marketing$categorie[pred.marketing == "moyenne"] <- "moyenne"
marketing$categorie[pred.marketing == "petit3"] <- "petit3"
marketing$categorie[pred.marketing == "petit5"] <- "petit5"
marketing$categorie[pred.marketing == "tresLongue"] <- "tresLongue"
View(marketing)
#--------------------------------#
# PLUS PROCHE VOISIN #
#--------------------------------#

install.packages("kknn")
library(kknn)

knn <-kknn(categorie ~ age + sexe + situationFamiliale + nbEnfantsAcharge + X2eme.voiture, categorie_EA[1:1000,],categorie_ET[1:1000,])

summary(knn)

table(categorie_ET$categorie, knn$fitted.values)


#----------------#
# RANDOM FORESTS #
#----------------#

install.packages("randomForest")
library(randomForest)

# Apprentissage du classifeur de type foret aleatoire
tree_rf <- randomForest(categorie~ age + sexe + situationFamiliale + nbEnfantsAcharge + X2eme.voiture, categorie_EA, na.action = na.roughfix)

# Test du classifieur : classe predite
rf_class <- predict(tree_rf,categorie_ET, type="response")
rf_class
table(rf_class)

# Matrice de confusion
table(categorie_ET$categorie, rf_class)


#-------------#
# NAIVE BAYES #
#-------------#

install.packages("naivebayes")
library(naivebayes)

# Apprentissage du classifeur de type naive bayes
tree_nb <- naive_bayes(categorie~ age + sexe + nbEnfantsAcharge + X2eme.voiture, categorie_EA)
tree_nb

# Test du classifieur : classe predite
nb_class <- predict(tree_nb, categorie_ET, type="class")
nb_class
table(nb_class)

# Matrice de confusion
table(categorie_ET$categorie, nb_class)

#-----------------#
# NEURAL NETWORKS #
#-----------------#

install.packages("nnet")
library(nnet)

#nnet always has one hidden layer

# Apprentissage du classifeur de type perceptron monocouche
tree_nn <- nnet(categorie~ age + sexe + situationFamiliale + nbEnfantsAcharge + X2eme.voiture, categorie_EA, size=50, MaxNWts = 905)
tree_nn

# Test du classifieur : classe predite
nn_class <- predict(tree_nn, categorie_ET, type="class")
nn_class
table(nn_class)

# Matrice de confusion
table(categorie_ET$categorie, nn_class)




# Test et taux de succes pour le 1er paramÃ©trage pour C5.0()
test_C51 <- predict(tree_C51, produit_ET, type="class")
print(taux_C51 <- nrow(produit_ET[produit_ET$Produit==test_C51,])/nrow(produit_ET))
