/*
 * Simulation of animals on a field with growing grass.
 * Animals can be Herbivores (Zebra), Carnivores (Lion), or Omnivores (Boar).
 * Each day, animals eat, hunt, and lose energy, while grass grows.
 * Implements all rules described in Assignment 4.
 */

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        final int maxDay = 30;
        final int minDay = 1;
        final float maxGrass = 100.0f;
        final float minGrass = 0.0f;
        final int maxAnimals = 20;
        final int minAnimals = 1;
        final float hundred = 100.0f;

        String file = "input.txt";
        int days = 0;
        float grass = 0.0f;
        int number = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if (line == null) { System.out.println("Invalid inputs"); return; }
            try { days = Integer.parseInt(line); } catch (Exception e) { System.out.println("Invalid inputs"); return; }

            line = br.readLine();
            if (line == null) { System.out.println("Invalid inputs"); return; }
            grass = parseFloat(line);
            if (grass < minGrass || grass > maxGrass) { System.out.println("The grass is out of bounds"); return; }

            if (days < minDay || days > maxDay) { System.out.println("Invalid inputs"); return; }

            line = br.readLine();
            if (line == null) { System.out.println("Invalid inputs"); return; }
            try { number = Integer.parseInt(line); } catch (Exception e) { System.out.println("Invalid inputs"); return; }
            if (number < minAnimals || number > maxAnimals) { System.out.println("Invalid inputs"); return; }

        } catch (IOException e) { System.out.println("Invalid inputs"); return; }

        List<Animal> animals = readAnimals(file, number);
        if (animals.size() != number) { System.out.println("Invalid inputs"); return; }

        Field field = new Field(Math.min(hundred, Math.max(0.0f, grass)));
        runSimulation(days, field, animals);
        printAnimals(animals);
    }

    private static float parseFloat(String value) {
        if (value != null && value.endsWith("F")) value = value.substring(0, value.length() - 1);
        return Float.parseFloat(value);
    }

    private static List<Animal> readAnimals(String filePath, int numbers) {
        final int lengthOfCommand = 4;
        final int tri = 3;
        List<Animal> animals = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); br.readLine(); br.readLine(); // skip first 3 lines

            for (int i = 0; i < numbers; i++) {
                String line = br.readLine();
                if (line == null) { System.out.println("Invalid inputs"); System.exit(0); }
                String[] tokens = line.split("\\s+");
                if (tokens.length != lengthOfCommand) { System.out.println("Invalid number of animal parameters"); System.exit(0); }

                String type = tokens[0];
                float weight, speed, energy;

                try { weight = parseFloat(tokens[1]); } catch (Exception e) { System.out.println("Invalid inputs"); System.exit(0); return animals; }
                try { speed = parseFloat(tokens[2]); } catch (Exception e) { System.out.println("Invalid inputs"); System.exit(0); return animals; }
                try { energy = parseFloat(tokens[tri]); } catch (Exception e) { System.out.println("Invalid inputs"); System.exit(0); return animals; }

                if (!type.equals("Lion") && !type.equals("Zebra") && !type.equals("Boar")) { System.out.println("Invalid inputs"); System.exit(0); }
                if (weight < Animal.MIN_WEIGHT || weight > Animal.MAX_WEIGHT) { System.out.println("The weight is out of bounds"); System.exit(0); }
                if (speed < Animal.MIN_SPEED || speed > Animal.MAX_SPEED) { System.out.println("The speed is out of bounds"); System.exit(0); }
                if (energy < Animal.MIN_ENERGY || energy > Animal.MAX_ENERGY) { System.out.println("The energy is out of bounds"); System.exit(0); }

                Animal a;
                switch (type) {
                    case "Lion": a = new Lion(weight, speed, energy); break;
                    case "Zebra": a = new Zebra(weight, speed, energy); break;
                    case "Boar": a = new Boar(weight, speed, energy); break;
                    default: System.out.println("Invalid inputs"); System.exit(0); return animals;
                }
                animals.add(a);
            }
        } catch (IOException e) { System.out.println("Invalid inputs"); System.exit(0); }

        return animals;
    }

    private static void runSimulation(int days, Field field, List<Animal> animals) {
        for (int d = 0; d < days; d++) {
            removeDeadAnimals(animals);
            int size = animals.size();
            if (size == 0) { field.makeGrassGrow(); continue; }

            for (int i = 0; i < size; i++) {
                if (i >= animals.size()) break;
                Animal current = animals.get(i);
                if (!current.isAlive()) continue;
                current.eat(animals, field);
            }

            for (Animal a : animals) a.decrementEnergy();
            removeDeadAnimals(animals);
            field.makeGrassGrow();
        }
    }

    private static void removeDeadAnimals(List<Animal> animals) { animals.removeIf(a -> !a.isAlive()); }
    private static void printAnimals(List<Animal> animals) { for (Animal a : animals) System.out.println(a.makeSound()); }
}

// ------------------- Animal classes and interfaces -------------------
abstract class Animal {
    public static final float MIN_SPEED = 5f, MAX_SPEED = 60f;
    public static final float MIN_ENERGY = 0f, MAX_ENERGY = 100f;
    public static final float MIN_WEIGHT = 5f, MAX_WEIGHT = 200f;

    private float weight, speed, energy;

    protected Animal(float weight, float speed, float energy) { this.weight = weight; this.speed = speed; this.energy = energy; }

    public float getWeight() { return weight; }
    public float getSpeed() { return speed; }
    public float getEnergy() { return energy; }
    public void setEnergy(float energy) { this.energy = Math.max(0, Math.min(100, energy)); }
    public boolean isAlive() { return energy > 0; }
    public void decrementEnergy() { setEnergy(this.energy - 1f); }
    public abstract String makeSound();
    public void eat(List<Animal> animals, Field field) { }
}

interface Carnivore {
    static Animal choosePrey(List<Animal> animals, Animal hunter) {
        int idx = animals.indexOf(hunter);
        if (idx == -1 || animals.size() == 0) return null;
        int preyIndex = (idx + 1) % animals.size();
        return animals.get(preyIndex);
    }

    static void huntPrey(Animal hunter, Animal prey) {
        try {
            if (prey == hunter) throw new SelfHuntingException();
            if (hunter.getClass().equals(prey.getClass())) throw new CannibalismException();
            if (!(prey.getSpeed() < hunter.getSpeed() || prey.getEnergy() < hunter.getEnergy())) throw new TooStrongPreyException();

            hunter.setEnergy(Math.min(100, hunter.getEnergy() + prey.getWeight()));
            prey.setEnergy(0);
        } catch (Exception e) { System.out.println(e.getMessage()); }
    }
}

interface Herbivore {
    static void grazeInTheField(Animal grazer, Field field) {
        float requiredGrass = grazer.getWeight() * 1.1f;
        if (field.getGrassAmount() >= requiredGrass) {
            field.setGrassAmount(field.getGrassAmount() - requiredGrass);
            grazer.setEnergy(Math.min(100, grazer.getEnergy() + requiredGrass));
        }
    }
}

interface Omnivore extends Herbivore { }

class Lion extends Animal implements Carnivore {
    public Lion(float weight, float speed, float energy) { super(weight, speed, energy); }
    public String makeSound() { return AnimalSound.LION.getSound(); }
    public void eat(List<Animal> animals, Field field) {
        if (!isAlive()) return;
        Animal prey = Carnivore.choosePrey(animals, this);
        if (prey != null && prey.isAlive()) Carnivore.huntPrey(this, prey);
    }
}

class Zebra extends Animal implements Herbivore {
    public Zebra(float weight, float speed, float energy) { super(weight, speed, energy); }
    public String makeSound() { return AnimalSound.ZEBRA.getSound(); }
    public void eat(List<Animal> animals, Field field) { if (!isAlive()) return; Herbivore.grazeInTheField(this, field); }
}

class Boar extends Animal implements Omnivore, Carnivore {
    public Boar(float weight, float speed, float energy) { super(weight, speed, energy); }
    public String makeSound() { return AnimalSound.BOAR.getSound(); }
    public void eat(List<Animal> animals, Field field) {
        if (!isAlive()) return;
        Herbivore.grazeInTheField(this, field);
        Animal prey = Carnivore.choosePrey(animals, this);
        if (prey != null && prey.isAlive()) Carnivore.huntPrey(this, prey);
    }
}

// ------------------- Field & Enums -------------------
class Field {
    private float grassAmount;
    public Field(float grassAmount) { this.grassAmount = grassAmount; }
    public float getGrassAmount() { return grassAmount; }
    public void setGrassAmount(float grassAmount) { this.grassAmount = Math.max(0, Math.min(100, grassAmount)); }
    public void makeGrassGrow() { setGrassAmount(grassAmount * 2); }
}

enum AnimalSound { LION("Roar"), ZEBRA("Ihoho"), BOAR("Oink");
    private final String sound;
    AnimalSound(String sound) { this.sound = sound; }
    public String getSound() { return sound; }
}

// ------------------- Exceptions -------------------
class GrassOutOfBoundsException extends Exception { public String getMessage() { return "The grass is out of bounds"; } }
class InvalidNumberOfAnimalParametersException extends Exception { public String getMessage() { return "Invalid number of animal parameters"; } }
class InvalidInputsException extends Exception { public String getMessage() { return "Invalid inputs"; } }
class WeightOutOfBoundsException extends Exception { public String getMessage() { return "The weight is out of bounds"; } }
class SpeedOutOfBoundsException extends Exception { public String getMessage() { return "The speed is out of bounds"; } }
class EnergyOutOfBoundsException extends Exception { public String getMessage() { return "The energy is out of bounds"; } }
class SelfHuntingException extends Exception { public String getMessage() { return "Self-hunting is not allowed"; } }
class CannibalismException extends Exception { public String getMessage() { return "Cannibalism is not allowed"; } }
class TooStrongPreyException extends Exception { public String getMessage() { return "The prey is too strong or too fast to attack"; } }
