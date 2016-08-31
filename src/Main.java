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
    }

    List<Chromosome> completeSet = new ArrayList<>();

    String inputFile = "SalData.xls";
    double[] min, max;
    int numberOfAttributes = 8;

    private void read() throws IOException {

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
                completeSet.add(new Chromosome(newVector));
            }
        } catch (BiffException e) {
            e.printStackTrace();
        }
        normalizeAll();
    }

    private void normalizeAll() {
        for (Chromosome chromosome : completeSet) {
            double[] newVector = chromosome.attributes;
            for (int i = 0; i < numberOfAttributes; i++) {
                newVector[i] = getNormalizedValue(newVector[i], min[i], max[i]);
            }
            chromosome.attributes = newVector;
        }
    }

    private double getNormalizedValue(double value, double min, double max) {
        return (value - min) / (max - min);
    }
}
