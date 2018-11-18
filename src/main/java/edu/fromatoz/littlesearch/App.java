package edu.fromatoz.littlesearch;

import java.util.List;
import java.util.ListIterator;

import org.apache.lucene.document.Document;

/**
 * This class contains the main method of the application:
 * When we use Littlesearch, the application tries to index the texts and,
 * if the indexing is successful, search for the words entered by the user
 * (in the texts indexed).
 * 
 * @author Andrei Zabolotnîi
 * @author Cyril Marilier
 */
public class App {

	// TODO: To make a corpus of real texts.
	private static final String TEXT_HENRI_POINCARE =
			"\tHenri Poincaré est un mathématicien, physicien, philosophe et ingénieur français, né le 29 avril 1854 à\n"
			+ "\tNancy et mort le 17 juillet 1912 à Paris.\n"
			+ "\n\tPoincaré a réalisé des travaux d'importance majeure en optique et en calcul infinitésimal. Ses avancées\n"
			+ "\tsur le problème des trois corps en font un fondateur de l'étude qualitative des systèmes d'équations\n"
			+ "\tdifférentielles et de la théorie du chaos ; il est aussi un précurseur majeur de la théorie de la relativité\n"
			+ "\trestreinte et de la théorie des systèmes dynamiques.\n"
			+ "\n\tIl est considéré comme un des derniers grands savants universels, maîtrisant l'ensemble des branches\n"
			+ "\tdes mathématiques de son époque et certaines branches de la physique.\n";

	private static final String TEXT_BERNHARD_RIEMANN =
			"\tGeorg Friedrich Bernhard Riemann, né le 17 septembre 1826 à Breselenz, État de Hanovre, mort le\n"
			+ "\t20 juillet 1866 à Selasca, hameau de la commune de Verbania, Italie, est un mathématicien allemand.\n"
			+ "\tInfluent sur le plan théorique, il a apporté de nombreuses contributions importantes à l'analyse et à la\n"
			+ "\tgéométrie différentielle, certaines d'entre elles ayant permis par la suite le développement de la relativité\n"
			+ "\tgénérale.\n";

	public static void main(String[] args) {

		// Allows to get the words to be searched by the command.
		String words = "";
		if (args.length > 0) {
			for (String word : args) {
				words += word + " ";
			}
		} else {
			words = "mathématicien systèmes";
		}

		// Indexes the texts...
		if (!Littlesearch.index(TEXT_HENRI_POINCARE, TEXT_BERNHARD_RIEMANN)) {
			System.exit(1);
		} else {
			// If the indexing is successful, searches words...
			List<Document> hitDocuments = Littlesearch.search(words);
			if (hitDocuments.isEmpty()) {
				System.out.println("Littlesearch n'a rien trouvé pour \"" + words + "\".");
				System.exit(1);
			} else {
				// If the engine finds at least one of the searched words in an indexed document, returns the document in question...
				ListIterator<Document> hitDocsIterator = hitDocuments.listIterator();
				int i = 0;
				while(hitDocsIterator.hasNext()) {
					Document hitDocument = hitDocsIterator.next();
					System.out.println("Document " + ++i + ":\n" + hitDocument.get("field_name"));
				}
			}
		}
	}

}
