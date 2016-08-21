# SceneBuilder_FR
JavaFX SceneBuilder translate in French (Traduction française de SceneBuilder pour JavaFX)
Ce projet à pour but de proposer l'application JavaFX SceneBuilder en version française. 

Tondeur Hervé (2016) 

[Blog TondeurH sur Developpez.com](http://www.developpez.net/forums/blogs/69141-tondeurh/)


SceneBuilder est sous licence BSD.

Cette version est modifié : 
- Elle ne nécessite pas d'enregistrement auprés de GluonHq.
- Elle affiche les infos de traduction dans la panneau à propos de...
- Elle est complémtement francisé et essaye de respecter les raccourcis claviers d'origines(quand celà est possible).

Comment compiler SceneBuilder en version française ?
==================================================
Prérequis :
==========
- JDK 8
- Graddle 2.2 ou +


Téléchargez les sources sur votre machine via l'outil de versionning git, éxécutez la commande suivante : 

*$> git clone https://github.com/tondeur-h/SceneBuilder_FR*

Ou via l'outils git de NetBeans 8 ou n'importe quel éditeur de code source Java qui prend en charge l'outil gradle.

Il suffit ensuite de lancer la compilation via la commande :

*$> cd SceneBuiler_FR*

*$> gradle assemble*

renommer le fichier qui se trouve dans le dossier *./SceneBuilder_FR/build/libs*

*$> cd ./build/libs*

*$> mv scenebuilder-all-8.2.0-FRENCH-all.jar dist.jar*

il ne reste plus qu'a remplacer le fichier dist.jar de la distribution GluonHq par ce nouveau fichier dist.jar en français.

**nb : Exemple pour une distribution Linux Debian**

*$> sudo cp ./dist.jar /opt/SceneBuilder/app*


Comment obtenir directement le binaire Jar de l'application en Français ?
========================================================================

il est en téléchargement directement a cette adresse :

[ScenBuilder FR](http://tondeurh.fr/software/SceneBuilder_FR/dist.jar)

Comment installer rapidement l'application ?
==========================================
Il faut installer la version US de chez GluonHq, téléchargez la version qui correspond à votre système d'exploitation :

[GluonHq SceneBuilder Binaires](http://gluonhq.com/labs/scene-builder/)

Cette distribution contient l'ensemble des binaires qui peremttent selon le système d'exploitation d'appeler directement SceneBuilder via un éxécutable ou via NetBeans ou tout autre éditeur de code source Java.

il ne reste plus qu'a remplacer le fichier dist.jar de la distribution GluonHq par ce nouveau fichier dist.jar en français.

**nb : Exemple pour une distribution Linux Debian**

*$> sudo cp ./dist.jar /opt/SceneBuilder/app*

Comment obtenir les fichiers sources origines de SceneBuilder chez GluonHq ?
==========================================================================

Juste en vous connectant sur atlassian BitBucket ([BitBucket](https://bitbucket.org))

et sur le repository du projet SceneBuilder de Gluon-OSS

[SceneBuilder Gluon-OSS](https://bitbucket.org/gluon-oss/scenebuilder)

Vous obtenez les derniéres sources mises à jours par la société Gluonhq.

En les remerciant pour ce travail...
