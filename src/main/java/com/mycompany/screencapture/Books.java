package com.mycompany.screencapture;

import java.util.ArrayList;

/**
 * Created by almatarm on 30/10/2019.
 */
public class Books {
    public static ArrayList<String> names = new ArrayList<>();

    static {
//        names.add("Beginner Calisthenics");
        names.add("Circadian");
        names.add("Circadian Rhythms and the Human");
        names.add("Craft Coffee - Jessica Easto and Andreas Willhoff");
        names.add("Eat Dirt");
        names.add("Exercise Medicine Physiological Principles and Clinical Applications");
        names.add("Exercise Samples");
//        names.add("Frank Lloyd Wright and Mason City - Roy R. Behrens");
        names.add("How To Win Friends and Influence People");
        names.add("How to Talk so Little Kids Will Listen");
//        names.add("Intuitive Eating - 30 Intuitive Eating Tips");
        names.add("Intuitive Eating A Revolutionary Program that Works");
        names.add("Intuitive Eating, 2nd Edition: A Revolutionary Program That Works");
        names.add("Mold-Mycotoxins-Current-Evaluation-and-Treatment-2016");
        names.add("Own the Day, Own Your Life");
        names.add("Paleo Workouts For Dummies");
        names.add("Photovoltaic Design and Installation For Dummies");
        names.add("Requiem for a Dream");
        names.add("She - A History of Adventure");
        names.add("Song of Kali");
        names.add("Summary of 7 Lessons from Heaven");
        names.add("Summary of Algorithms to Live By");
        names.add("Summary of Dr. Gundry's Diet Evolution");
        names.add("Summary of The Power of Habit");
        names.add("Summary of Why We Sleep");
//        names.add("The Elements of Style");
        names.add("The Good Earth");
        names.add("The Power of Posture");
        names.add("The Social Animal");
        names.add("The World As I See It");
        names.add("Why We Sleep");
        names.add("Yoga Assists");
        names.add("The Autoimmune Solution | Amy Myers");
        names.add("Rosemary Gladstar's Medicinal Herbs | Rosemary Gladstar");
        names.add("Reiki for Beginners | David Vennells");
        names.add("Dysautonomia, POTS Syndrome | Frederick Earlstein");
    }

    public static ArrayList<String> getNames() {
        return names;
    }

    public static String getLastName() {
        return names.get(names.size() -1);
    }
}
