# SceneBuilder_FR
JavaFX SceneBuilder translate in French (Traduction française de SceneBuilder pour JavaFX)
Ce projet à pour but de proposer l'application JavaFX SceneBuilder en version française. 

Tondeur Hervé (2016) 

[Blog TondeurH sur Developpez.com](http://www.developpez.net/forums/blogs/69141-tondeurh/)


SceneBuilder est sous licence BSD.


Comment compiler SceneBuilder en version française ?
==================================================
Prérequis :
==========
- JDK 8
- Graddle 2.2 ou +


Téléchargez les sources sur votre machine via l'outil de versionning git, éxécutez la commande suivante : 

$> git clone https://github.com/tondeur-h/SceneBuilder_FR

Ou via l'outils git de NetBeans 8 ou n'importe quel éditeurs de code source Java qui prend en charge l'outil gradle.

Il suffit ensuite de lancer la compilation via la commande :

$> cd SceneBuiler_FR

$> gradle assemble

renommer le fichiers qui se trouve dans le dossier ./SceneBuilder_FR/build/libs
$> cd ./build/libs
$> mv scenebuilder-all-8.2.0-FRENCH-all.jar dist.jar

il ne reste plus qu'a remplacer le fichier dist.jar de la distribution GluonHq par ce nouveau fichier dist.jar en français.

Exemple sous une distribution Linux Debian :
==========================================

$> sudo cp ./dist.jar /opt/SceneBuilder/app


Comment obtenir directement le binaire Jar de l'application en Français ?
========================================================================


Comment installer rapidement l'application ?
==========================================
Il faut installer la version US de chez GluonHq, téléchargez la version qui correspond à votre système d'exploitation :
[GluonHq SceneBuilder Binaires](http://gluonhq.com/labs/scene-builder/)



Comment obtenir les sources origines de SceneBuilder chez GluonHq ?
==================================================================

Juste en vous connectant sur atlassian BitBucket ([BitBucket](https://bitbucket.org))

et sur le repository du projet SceneBuilder de Gluon-OSS

[SceneBuilder Gluon-OSS](https://bitbucket.org/gluon-oss/scenebuilder)

Vous obtenez les derniéres sources mises à jours par la société Gluonhq.

En les remerciant pour cet travail...
