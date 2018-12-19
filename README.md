Littlesearch
============

Littlesearch est avant tout le projet d'un **moteur de recherche** (`SearchEngine`) en Java.

Si nous avons décidé de le nommer ainsi, c'est tout d'abord en référence à Elasticsearch : non seulement, notre moteur de recherche, utilise-t-il, lui aussi, [Lucene](http://lucene.apache.org/), mais il se base, lui aussi, sur des informations hiérarchisées en JSON. Toutefois, la comparaison s'arrête là, Littlesearch étant un *petit* projet, d'autant que le nombre des documents dans lesquels notre moteur effectue les recherches est petit.

Dès lors, quel intérêt pouvait-il y avoir, au-delà de l'intérêt de savoir comment fonctionne un moteur de recherche, à développer un moteur de recherche aussi modeste que le nôtre mais basé sur les mêmes outils que certains de ceux mis en œuvre au cœur d'Elasticsearch ?

Littlesearch a été conçu pour du français ; or, en développant le projet, nous avons découvert une faiblesse de certains des outils Java proposés actuellement pour cette langue : par exemple, le [FrenchStemmer](https://lucene.apache.org/core/7_5_0/analyzers-common/org/tartarus/snowball/ext/FrenchStemmer.html) classiquement recommandé à ce jour ne permet pas toujours d'obtenir une valeur satisfaisante. (Nous avons écrit une classe pour voir un peu ce que l'outil en question nous retournait : la classe "StandardFrenchStemmerTest"...)

C'est pourquoi nous avons développé, à côté de notre moteur de recherche, un **intégrateur de données** (`DataIntegrator`).

Compilation
-----------

Pour compiler le code, le développeur utilise [Maven](https://maven.apache.org/) :

```sh
mvn clean package
```

Exécution
---------

Pour exécuter le programme de l'*intégrateur de données*, **le développeur** lance l'application à partir de l'éditeur.

Pour exécuter le programme du *moteur de recherche*, **l'utilisateur** lance le script shell "searchFor" :

```sh
./searchFor <words>... 
```

Par exemple, on souhaite rechercher le nom "Riemann" :

```sh
./searchFor "Riemann"
```

Il est aussi possible d'écrire :

```sh
./searchFor Riemann
```

Au cas où l'on souhaite rechercher plusieurs mots :

```sh
./searchFor "Riemann" "travail"
```

Il est aussi possible d'écrire :

```sh
./searchFor "Riemann travail"
```

Ou encore :

```sh
./searchFor Riemann travail
```

Par défaut, la recherche est effectuée par synonymie, autant que faire se peut (par exemple, le mot Riemann, étant un nom propre, n'aura aucun synonyme) ; si l'on veut rechercher un mot exact (avec l'orthographe saisie), alors il suffit d'entrer le mot en question entre tirets bas ("underscore") :

```sh
./searchFor Riemann _travaux_
```

Au cas où l'on veut rechercher plusieurs mots exacts :

```sh
./searchFor Riemann _travaux_ _mathématicienne_
```

Il est aussi possible d'écrire :

```sh
./searchFor "Riemann _travaux_ _mathématicienne_"
```

Ou encore :

```sh
./searchFor Riemann "_travaux_" _mathématicienne_
```

**Attention** : En revanche, il n'est pas encore possible de rechercher une expression (c'est-à-dire une suite mots) exacte...

Documentation
------------

Quelques diagrammes de classes, à propos de l'intégrateur de données notamment, permettent de mieux comprendre quelques moments d'architecture de notre code.

La [Javadoc]() a été rédigée avec beaucoup de sérieux et devrait fournir des informations précieuses sur le projet.

Auteurs
-------

L'équipe **fromAtoZ** :
* Andrei ZABOLOTNÎI
* Cyril MARILIER
