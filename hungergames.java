package games;

import java.util.ArrayList;

/**
 * This class contains methods to represent the Hunger Games using BSTs.
 * Moves people from input files to districts, eliminates people from the game,
 * and determines a possible winner.
 * 
 * @author Pranay Roni
 * @author Maksims Kurjanovics Kravcenko
 * @author Kal Pandit
 */
public class HungerGames {

    private ArrayList<District> districts;  // all districts in Panem.
    private TreeNode            game;       // root of the BST. The BST contains districts that are still in the game.

    /**
     * ***** DO NOT REMOVE OR UPDATE this method *********
     * Default constructor, initializes a list of districts.
     */
    public HungerGames() {
        districts = new ArrayList<>();
        game = null;
        StdRandom.setSeed(2023);
    }

    /**
     * ***** DO NOT REMOVE OR UPDATE this method *********
     * Sets up Panem, the universe in which the Hunger Games takes place.
     * Reads districts and people from the input file.
     * 
     * @param filename will be provided by client to read from using StdIn
     */
    public void setupPanem(String filename) { 
        StdIn.setFile(filename);  // open the file - happens only once here
        setupDistricts(filename); 
        setupPeople(filename);
    }

    /**
     * Reads the following from input file:
     * - Number of districts
     * - District ID's (insert in order of insertion)
     * Insert districts into the districts ArrayList in order of appearance.
     * 
     * @param filename will be provided by client to read from using StdIn
     */
    public void setupDistricts (String filename) {

        // WRITE YOUR CODE HERE
        StdIn.setFile(filename);
        int numOfDistricts = StdIn.readInt();
        for (int i = 0; i < numOfDistricts; i++) {
            districts.add(new District(StdIn.readInt()));
        }
    }

    /**
     * Reads the following from input file (continues to read from the SAME input file as setupDistricts()):
     * Number of people
     * Space-separated: first name, last name, birth month (1-12), age, district id, effectiveness
     * Districts will be initialized to the instance variable districts
     * 
     * Persons will be added to corresponding district in districts defined by districtID
     * 
     * @param filename will be provided by client to read from using StdIn
     */
    public void setupPeople (String filename) {

        // WRITE YOUR CODE HERE
        int numOfPlayers = StdIn.readInt();
        for (int i = 0; i < numOfPlayers; i++) {
            String firstName = StdIn.readString();
            String lastName = StdIn.readString();
            int birthMonth = StdIn.readInt();
            int age = StdIn.readInt();
            int districtID = StdIn.readInt();
            int effectiveness = StdIn.readInt();
            Person newPerson = new Person(birthMonth, firstName, lastName, age, districtID, effectiveness);
            if (age >= 12 && age < 18) {
                newPerson.setTessera(true);
            }
            for (int j = 0; j < districts.size(); j++) {
                if (districts.get(j).getDistrictID() == districtID) {
                    if (birthMonth % 2 == 0) {
                        districts.get(j).addEvenPerson(newPerson);
                    }
                    else {
                        districts.get(j).addOddPerson(newPerson);
                    }
                }
            }
        }
    }

    /**
     * Adds a district to the game BST.
     * If the district is already added, do nothing
     * 
     * @param root        the TreeNode root which we access all the added districts
     * @param newDistrict the district we wish to add
     */
    public void addDistrictToGame(TreeNode root, District newDistrict) {
        if (root == null) {
            districts.remove(newDistrict);
            game = new TreeNode(newDistrict, null, null);
            return;
        }
        if (root.getDistrict().getDistrictID() < newDistrict.getDistrictID()) {
            if (root.getRight() == null) {
                districts.remove(newDistrict);
               root.setRight(new TreeNode(newDistrict, null, null));
            } else {
                addDistrictToGame(root.getRight(), newDistrict);
            }
        }
        else {
            if (root.getLeft() == null) {
                districts.remove(newDistrict);
                root.setLeft(new TreeNode(newDistrict, null, null));
            } else {
                addDistrictToGame(root.getLeft(), newDistrict);
            }
        }
    }

    private TreeNode findTreeNode(TreeNode root, int id) {
        if (root == null) return null;
        if (root.getDistrict().getDistrictID() == id) {
            return root;
        }
        else {
            if (root.getDistrict().getDistrictID() < id) {
                if (root.getRight() == null) {
                    return null;
                } else {
                    return findTreeNode(root.getRight(), id);
                }
            }
            else {
                if (root.getLeft() == null) {
                    return null;
                } else {
                    return findTreeNode(root.getLeft(), id);
                }
            }
        }
        
    }
    /**
     * Searches for a district inside of the BST given the district id.
     * 
     * @param id the district to search
     * @return the district if found, null if not found
     */
    public District findDistrict(int id) {

        // WRITE YOUR CODE HERE
        TreeNode a = findTreeNode(game, id);
        if (a == null) return null;
        else {
            return a.getDistrict();
        }
    }

    private Person[] findDuelers(TreeNode root, Person[] people) {
        if (root == null) return people;
        boolean oddPersonFound = false;
        for (int i = 0; i < root.getDistrict().getOddPopulation().size(); i++) {
            if (root.getDistrict().getOddPopulation().get(i).getTessera() == true && people[0] == null) {
                people[0] = root.getDistrict().getOddPopulation().get(i);
                root.getDistrict().getOddPopulation().remove(people[0]);
                oddPersonFound = true;
            }
        }
        for (int i = 0; i < root.getDistrict().getEvenPopulation().size(); i++) {
            if (root.getDistrict().getEvenPopulation().get(i).getTessera() == true && people[1] == null && oddPersonFound == false) {
                people[1] = root.getDistrict().getEvenPopulation().get(i);
                root.getDistrict().getEvenPopulation().remove(people[1]);
                oddPersonFound = true;
            }
        }
        
        people = findDuelers(root.getLeft(), people);
        people = findDuelers(root.getRight(), people);
        return people;
    }
    private Person[] findDuelersNoTessera(TreeNode root, Person[] people) {
        if (root == null || (people[0] != null && people[1] != null)) return people;
        boolean oddPersonFound = false;
        if (people[0] == null) {
            Person p = root.getDistrict().getOddPopulation().get(StdRandom.uniform(root.getDistrict().getOddPopulation().size()));
            if (people[1] == null || p.getDistrictID() != people[1].getDistrictID()) {
                people[0] = p;
                root.getDistrict().getOddPopulation().remove(people[0]);
                oddPersonFound = true;
            }
        }
        if (people[1] == null && oddPersonFound == false) {
            Person p = root.getDistrict().getEvenPopulation().get(StdRandom.uniform(root.getDistrict().getEvenPopulation().size()));
            if (people[0] == null || p.getDistrictID() != people[0].getDistrictID()) {
                people[1] = p;
                root.getDistrict().getEvenPopulation().remove(people[1]);
            }
        }
        if (people[0] != null && people[1] != null) return people;
        people = findDuelersNoTessera(root.getLeft(), people);
        people = findDuelersNoTessera(root.getRight(), people);
        return people;
    }

    /**
     * Selects two duelers from the tree, following these rules:
     * - One odd person and one even person should be in the pair.
     * - Dueler with Tessera (age 12-18, use tessera instance variable) must be
     * retrieved first.
     * - Find the first odd person and even person (separately) with Tessera if they
     * exist.
     * - If you can't find a person, use StdRandom.uniform(x) where x is the respective 
     * population size to obtain a dueler.
     * - Add odd person dueler to person1 of new DuelerPair and even person dueler to
     * person2.
     * - People from the same district cannot fight against each other.
     * 
     * @return the pair of dueler retrieved from this method.
     */
    public DuelPair selectDuelers() {
        Person[] people = new Person[2];
        people = findDuelers(game, people);
        people = findDuelersNoTessera(game, people);
        return new DuelPair(people[0], people[1]);
    }

    private TreeNode minimumNode(TreeNode root) {
        while (root.getLeft() != null) {
            root = root.getLeft();
        }
        return root;
    }

    private TreeNode findPreviousTreeNode(TreeNode root, int id) {
        if (root == null) return null;
        if (root.getDistrict().getDistrictID() == id) {
            return null;
        }
        if (root.getLeft() != null && root.getLeft().getDistrict().getDistrictID() == id || root.getRight() != null && root.getRight().getDistrict().getDistrictID() == id) return root;
        else {
            if (root.getDistrict().getDistrictID() < id) {
                if (root.getRight() == null) {
                    return null;
                } else {
                    return findPreviousTreeNode(root.getRight(), id);
                }
            }
            else {
                if (root.getLeft() == null) {
                    return null;
                } else {
                    return findPreviousTreeNode(root.getLeft(), id);
                }
            }
        }
        
    }
    /**
     * Deletes a district from the BST when they are eliminated from the game.
     * Districts are identified by id's.
     * If district does not exist, do nothing.
     * 
     * This is similar to the BST delete we have seen in class.
     * 
     * @param id the ID of the district to eliminate
     */
    public void eliminateDistrict(int id) {

        TreeNode districtToRemove = findTreeNode(game, id);
        if (districtToRemove == null) return;
        if (districtToRemove.getLeft() == null && districtToRemove.getRight() == null) {
            if (game.getDistrict().getDistrictID() == id) {
                game = null;
                return;
            }
            TreeNode parent = findPreviousTreeNode(game, id);
            if (parent.getLeft() == districtToRemove) {
                parent.setLeft(null);
            }
            else {
                parent.setRight(null);
            }
        }
        else if (districtToRemove.getLeft() != null && districtToRemove.getRight() == null) {
            TreeNode insert = districtToRemove.getLeft();
            districtToRemove.setDistrict(insert.getDistrict());
            districtToRemove.setRight(insert.getRight());;
            districtToRemove.setLeft(insert.getLeft());
        }
        else if (districtToRemove.getLeft() == null && districtToRemove.getRight() != null) {
            TreeNode insert = districtToRemove.getRight();
            districtToRemove.setDistrict(insert.getDistrict());
            districtToRemove.setRight(insert.getRight());;
            districtToRemove.setLeft(insert.getLeft());
        }
        else {
            TreeNode insert = minimumNode(districtToRemove.getRight());
            eliminateDistrict(insert.getDistrict().getDistrictID());
            if (game.getDistrict().getDistrictID() != id) {
                districtToRemove.setRight(insert.getRight());
                districtToRemove.setLeft(insert.getLeft());
            }
            districtToRemove.setDistrict(insert.getDistrict());
        }
        if (game.getDistrict().getDistrictID() == id) {
            game = districtToRemove;
        }
    }

    private void returnDuelerToDistrict(Person winner, Person loser) {
        if (winner == null) return;
        if (winner.getBirthMonth() % 2 != 0) {
            findDistrict(winner.getDistrictID()).addOddPerson(winner);
        }
        else {
            findDistrict(winner.getDistrictID()).addEvenPerson(winner);
        }
        if (loser != null) {
            District loserDistrict = findDistrict(loser.getDistrictID());
            if (loserDistrict.getEvenPopulation().size() == 0 || loserDistrict.getOddPopulation().size() == 0) {
                eliminateDistrict(loserDistrict.getDistrictID());
            }
        
        }
        
    }
    /**
     * Eliminates a dueler from a pair of duelers.
     * - Both duelers in the DuelPair argument given will duel
     * - Winner gets returned to their District
     * - Eliminate a District if it only contains a odd person population or even
     * person population
     * 
     * @param pair of persons to fight each other.
     */
    public void eliminateDueler(DuelPair pair) {
        if (pair.getPerson1() == null) {
            returnDuelerToDistrict(pair.getPerson2(), null);
        }
        else if (pair.getPerson2() == null) {
            returnDuelerToDistrict(pair.getPerson1(), null);
        }
        else {
            Person p = pair.getPerson1().duel(pair.getPerson2());
            if (p == pair.getPerson1()) returnDuelerToDistrict(p, pair.getPerson2());
            else returnDuelerToDistrict(p, pair.getPerson1());
        }
    }

    /**
     * ***** DO NOT REMOVE OR UPDATE this method *********
     * 
     * Obtains the list of districts for the Driver.
     * 
     * @return the ArrayList of districts for selection
     */
    public ArrayList<District> getDistricts() {
        return this.districts;
    }

    /**
     * ***** DO NOT REMOVE OR UPDATE this method *********
     * 
     * Returns the root of the BST
     */
    public TreeNode getRoot() {
        return game;
    }
}