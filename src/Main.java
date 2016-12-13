import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by s213391244 on 2016/08/31.
 */
public class Main {
    private List<Item> allItems;
    private GeneticAlgorithm geneticAlgorithm;
    private DifferentialEvolution differentialEvolution;
    private PSO pso;
    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        try {
            read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        geneticAlgorithm = new GeneticAlgorithm();
        geneticAlgorithm.run(allItems, 1000000);
/*
        differentialEvolution = new DifferentialEvolution();
        differentialEvolution.run(allItems, 2000000);
        /*
        pso = new PSO();
        pso.run(allItems, 100000);
    */
    }


    //private List<Component> allComponents;

    String inputFile = "DietProblem.xls";
    //String evalFile = "Evaluation.xls";

    private void read() throws IOException {

        allItems = new ArrayList<>();
        File inputWorkbook = new File(inputFile);
        Workbook workbook;

        try {
            workbook = Workbook.getWorkbook(inputWorkbook);
            Sheet sheet1 = workbook.getSheet(0);
            //Sheet sheet2 = workbook.getSheet(1);
            for (int row = 1; row < 78; row++) {
                Cell itemNameCell = sheet1.getCell(0, row);
                List<Component> itemComponents = new ArrayList<>();
                for (int col = 1; col < 11; col++) {
                    Cell componentNameCell = sheet1.getCell(col, 0);
                    Cell componentAmountCell = sheet1.getCell(col, row);
                    Cell recommendedAnuallyCell = sheet1.getCell(2, col + 78);
                    Cell unitCell = sheet1.getCell(3, col + 78);
                    String name = componentNameCell.getContents();
                    String units = unitCell.getContents();
                    String amount = componentAmountCell.getContents();
                    String annual = recommendedAnuallyCell.getContents();
                    try {
                        Component newComponent = new Component(name, amount, annual, units);
                        itemComponents.add(newComponent);
                    } catch (NumberFormatException e) {
                        System.out.print("");
                    }
                }
                Item newItem = new Item(itemNameCell.getContents(), itemComponents);
                allItems.add(newItem);
            }

        } catch (
                BiffException e)

        {
            e.printStackTrace();
        }
    }
}
