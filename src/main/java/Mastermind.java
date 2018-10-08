

import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.StringUtils;
import org.chocosolver.solver.Model;

import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class Mastermind {

    // 10 guesses already generate.
    // Savoir si on peut avoir au moins une solution qui existe
    // Modèle c'est ce qui va décrire le problème. Comment on explique au solveur le problème à résoudre.
    // heuristique : comment on le résoud. Quelle variabl choisir ? par où commencer ?
    public int nbGoodPos = 0;
    public int nbBadPos = 0;
    public int numberOfVariables = 10;
    public int sizeAnwers = 4;
    public int numberOfGuesses = 20;
    public int currentNumberOfGuesses = 0;
    public boolean duplicate = false;
    public int [] tabOfGoodPosB = new int[numberOfGuesses];
    public int [] tabOfBadPosWB = new int[numberOfGuesses];
    public int [] tabOfBadAndGoodW = new int[numberOfGuesses];
    public HashMap<String,String> tabOfGuessX = new HashMap<String,String>();


    public HashMap<String,Integer> countGuess = new HashMap<String,Integer>();

    public class variable{
        IntVar b ;
        IntVar wb;

        public void init(Model model)
        {
            b = model.intVar("b",-1);
            wb = model.intVar("wb", -1);
        }
    }

    public String generateNumber(int numberOfVariables, int  sizeAnswers){

        String randomNumber = "";
        if (duplicate)
        {
            Random generator = new Random();
            for( int i = 0; i < sizeAnswers ; ++i)
            {
                int number = generator.nextInt(numberOfVariables);
                randomNumber += number;
            }
        }else
        {
            Random generator = new Random();
            int [] checkIfIn = new int[sizeAnwers];
            Boolean alreadyIn = false;

            for( int i = 0; i < sizeAnswers ; ++i)
            {
                int number = generator.nextInt(numberOfVariables);
                while(isAlreadyIn(checkIfIn,alreadyIn,number))
                {
                    number = generator.nextInt(numberOfVariables);
                }
                checkIfIn[i] = number;
                randomNumber += number;
            }

        }
        //System.out.println("Random number : " + randomNumber);
        return randomNumber;
    }

    public boolean isAlreadyIn (int [] checkIfIn,boolean alreadyIn,int number)
    {

        for(int j = 0;j< checkIfIn.length;++j)
        {
            if(checkIfIn[j] == number)
            {
                alreadyIn = true;
            }
        }
        return alreadyIn;
    }

    public void play() {
        String answer = generateNumber(numberOfVariables,sizeAnwers);

        System.out.println("Welcome to Mastermind!");
        System.out.printf("I'm thinking of a %d digit code, with numbers between 0 and %d.\n", sizeAnwers, numberOfVariables-1);
        System.out.printf("Can you break the code in just %d guesses?\n", numberOfGuesses);

        boolean winner = false;
        Scanner input = new Scanner(System.in);
        while (currentNumberOfGuesses < numberOfGuesses) {
            System.out.printf("Guess %d: ", currentNumberOfGuesses + 1);
            String guess = input.nextLine();
            if(isBetweenVar(guess,answer) && guess.length() == answer.length())
            {
                initHashMap(countGuess);
                // Check the current guess, and exit if it is a perfect match
                if (numberOfCorecctAnswer(guess, answer))
                {
                    winner = true;
                    break;
                }
                currentNumberOfGuesses++;
            }else
            {
                System.out.printf("Attention, vous devez choisir un nombre à %d digit avec des chiffres entre 0 et %d.\n",sizeAnwers,numberOfVariables-1);
            }
        }
        String endGameMessage = winner ? "You solved it!" : "You lose :(";
        System.out.println(endGameMessage);
        System.out.println("Solution : " + answer);

    }


    // CHOCO FUNCTION
    public void playSolver(String answer){
        // answer = generateNumber(numberOfVariables,sizeAnwers);


        for (int i = 0 ; i <numberOfGuesses ; ++i)
        {
            initHashMap(countGuess);
            String guess = generateNumber(numberOfVariables,sizeAnwers);
            numberOfCorecctAnswer(guess,answer);
            tabOfGoodPosB[i] = nbGoodPos;
            tabOfBadPosWB[i] = nbBadPos;
            tabOfBadAndGoodW[i] = nbBadPos + nbGoodPos;
            tabOfGuessX.put(guess+","+answer,tabOfGoodPosB[i]+","+tabOfBadPosWB[i]);
            System.out.println("Guess = " + guess);
            System.out.println("Answer = " + answer);
            System.out.println("Fonction : (x,y) = "+ "("+tabOfGuessX.get(guess+","+answer)+")");
            // couple (x,y)) = (b,w-b) ou x = guess, y = answer, b = goodPos, w-b = badPos
            System.out.println("");
        }

    }

    // CHOCO FUNCTION
    public void modelMastermind()
    {
        String answer = generateNumber(numberOfVariables,sizeAnwers);
        playSolver(answer);
        Model model = new Model("Static Mastermind");
        //IntVar objective = model.intVar("objective",sizeAnwers);
        System.out.println("On a :" + (int)Math.pow(10,sizeAnwers));
        IntVar[] guessModel = model.intVarArray("Guess",(int)Math.pow(10,sizeAnwers),0,(int)Math.pow(10,sizeAnwers));

        for(int j = 0; j< Math.pow(10,sizeAnwers);j++) {
            variable varMod = numberOfCorrectAnswerModel(Integer.toString(guessModel[j].getValue()), answer, model);
            System.out.println("IM HERE BIATCH " + guessModel[j].getValue());

            for (int i = 0; i < numberOfGuesses; ++i) {
                model.arithm(varMod.b, "=", tabOfGoodPosB[i]).post();
                model.arithm(varMod.wb, "=", tabOfBadPosWB[i]).post();
            }
            System.out.println("AND HERE ?" + varMod.b + " et " + varMod.wb);
            model.getSolver().solve();
            System.out.println(guessModel);

        }


        //IntVar guessModel = model.intVar("guess",Integer.parseInt(guess));
        //IntVar answerModel = model.intVar("answer",Integer.parseInt(answer));

        //IntVar [] guessModelParse = new IntVar[guess.length()];
        //IntVar [] answerModelParse = new IntVar[answer.length()];
        //IntVar [] countGuessModel = new IntVar[answer.length()];
        //int [] tmp = parseInteger(guess);
        //int [] tmp2 = parseInteger(answer);
        //for(int i = 0 ; i<guess.length();++i)
        //{
        //    guessModelParse[i] = model.intVar("C_"+i,tmp[i]);
        //    answerModelParse[i] = model.intVar("C_"+i,tmp2[i]);
            //countGuessModel[i] = model.intVar("Guess"+i,0);
        //}


    }

    //CHOCO FUNCTION
    public int[] parseInteger(String x)
    {
        int [] y = new int[x.length()];
        int i = 0;
        while (Integer.parseInt(x) > 0) {
             y[i] = Integer.parseInt(x) / 10;
             ++i;
        }
        return y;
    }

    //CHOCO FUNCTION
    public int parseIntVar(IntVar x)
    {
        int cpt = 0;
        int tmp = x.getValue();
        while(tmp > 0)
        {
            tmp = tmp/10;
            cpt ++;
        }

        return cpt;
    }


    public Boolean isBetweenVar(String guess,String answer)
    {
        String [] guessVar = guess.split("");
        boolean isBeetwenVar = false;
        int nbGoodVar = 0;
        for(int i =0; i<guess.length();++i)
        {
            if(Integer.parseInt(guessVar[i]) < numberOfVariables && Integer.parseInt(guessVar[i]) >= 0)
            {
                nbGoodVar += 1;
            }
        }
        if (nbGoodVar == answer.length())
        {
            isBeetwenVar = true;
        }
        return isBeetwenVar;
    }

    public Boolean numberOfCorecctAnswer(String guess, String answer)
    {

        String [] guessParse = guess.split("");
        String [] answerParse = answer.split("");

        fillHashTableForGoodGuess(guess,answer,countGuess);
        nbBadPos = 0;
        nbGoodPos = 0;
        for(int i =0; i< numberOfVariables;++i)
        {
            String delims = "[,]";
            String[] tokens = numberOfOcurrence(Integer.toString(i),guess,answer).split(delims);
            int nbInGuess = Integer.parseInt(tokens[0]);
            int nbInAnswer = Integer.parseInt(tokens[1]);
            int numberInGuessTooMuch = nbInGuess - countGuess.get(Integer.toString(i));
            int numberInAnswerNotFound = nbInAnswer - countGuess.get(Integer.toString(i));
            //int numberBadPos = numberInGuessTooMuch - numberInAnswerNotFound;
            if(numberInGuessTooMuch >= numberInAnswerNotFound)
            {
                nbBadPos += numberInAnswerNotFound;
            }else
            {
                nbBadPos += numberInGuessTooMuch;
            }
        }

        for (int i = 0 ; i < guess.length(); ++i)
        {

            if (answerParse[i].equals(guessParse[i]))
            {
                nbGoodPos += 1;
            }
        }
        if(nbGoodPos == guess.length())
        {
            return true;
        }
        System.out.println("Il y a " + nbGoodPos + " chiffre présent bien placé !");
        System.out.println("Il y a " + nbBadPos + " chiffre présent mais mal placé !");
        return false;
    }

    // CHOCO FUNCTION
    public variable numberOfCorrectAnswerModel(String guess, String answer,Model model)
    {

        String [] guessParse = guess.split("");
        String [] answerParse = answer.split("");

        fillHashTableForGoodGuess(guess,answer,countGuess);
        nbBadPos = 0;
        nbGoodPos = 0;
        for(int i =0; i< numberOfVariables;++i)
        {
            String delims = "[,]";
            String[] tokens = numberOfOcurrence(Integer.toString(i),guess,answer).split(delims);
            int nbInGuess = Integer.parseInt(tokens[0]);
            int nbInAnswer = Integer.parseInt(tokens[1]);
            int numberInGuessTooMuch = nbInGuess - countGuess.get(Integer.toString(i));
            int numberInAnswerNotFound = nbInAnswer - countGuess.get(Integer.toString(i));
            //int numberBadPos = numberInGuessTooMuch - numberInAnswerNotFound;
            if(numberInGuessTooMuch >= numberInAnswerNotFound)
            {
                nbBadPos += numberInAnswerNotFound;
            }else
            {
                nbBadPos += numberInGuessTooMuch;
            }
        }

        for (int i = 0 ; i < guess.length(); ++i)
        {

            if (answerParse[i].equals(guessParse[i]))
            {
                nbGoodPos += 1;
            }
        }
        variable var = new variable();
        var.b = model.intVar("b", nbGoodPos);
        var.wb = model.intVar("wb", nbBadPos);

        //System.out.println("Il y a " + nbGoodPos + " chiffre présent bien placé !");
        //System.out.println("Il y a " + nbBadPos + " chiffre présent mais mal placé !");
        return var;
    }

    public String numberOfOcurrence(String variable,String guess,String answer)
    {
        int countGuess = guess.length() - guess.replace(variable,"").length();
        int countAnswer = answer.length() - answer.replace(variable,"").length();
        return countGuess+","+countAnswer;
    }

    public void initHashMap(HashMap<String,Integer> countGuess)
    {

        for(int i = 0;i<numberOfVariables; ++i)
        {
            countGuess.put(Integer.toString(i),0);
        }
    }
    public void fillHashTableForGoodGuess(String guess,String answer,HashMap<String,Integer> countGuess)
    {
        String [] guessParse = guess.split("");
        String [] answerParse = answer.split("");

        for (int i = 0; i< guess.length();++i)
        {
            if (answerParse[i].equals(guessParse[i]))
            {
                int value = countGuess.get(answerParse[i]);
                countGuess.put(answerParse[i],value+1);
            }
        }
    }
}

