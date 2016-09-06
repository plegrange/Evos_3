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

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        try {
            read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        populateLists();
        geneticAlgorithm = new GeneticAlgorithm(training, testing, min, max);
        geneticAlgorithm.run(min,max);
    }

    GeneticAlgorithm geneticAlgorithm;
    List<double[]> completeSet, training, testing;

    String inputFile = "SalData.xls";
    double[] min, max;
    int numberOfAttributes = 8;

    private void read() throws IOException {
        completeSet = new ArrayList<>();
        min = new double[numberOfAttributes];
        max = new double[numberOfAttributes];
        for (int i = 0; i < numberOfAttributes; i++) {
            min[i] = 99999;
            max[i] = -99999;
        }
        completeSet = new ArrayList<>();
        File inputWorkbook = new File(inputFile);
        Workbook workbook;

        try {
            workbook = Workbook.getWorkbook(inputWorkbook);
            Sheet sheet = workbook.getSheet(0);
            double[] newVector;
            for (int y = 1; y < 2001; y++) {
                newVector = new double[numberOfAttributes];

                for (int x = 0; x < numberOfAttributes; x++) {
                    Cell cell = sheet.getCell(x, y);
                    newVector[x] = Integer.parseInt(cell.getContents());
                    if (newVector[x] < min[x])
                        min[x] = newVector[x];
                    if (newVector[x] > max[x])
                        max[x] = newVector[x];
                }
                completeSet.add(newVector);
            }
        } catch (BiffException e) {
            e.printStackTrace();
        }
    }

    private void populateLists() {
        training = new ArrayList<>();
        for (int i = 0; i < 1900; i++) {
            training.add(completeSet.get(i));
        }
        testing = new ArrayList<>();
        for (int i = 1900; i < 2000; i++) {
            testing.add(completeSet.get(i));
        }
    }
}
