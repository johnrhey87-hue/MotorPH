/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.motorph;

/**
 *
 * @author JSC
 */
import java.io.*;
import java.util.*;
import java.text.*;

public class MotorPH {

    // Scanner for user input
    static Scanner input = new Scanner(System.in);

    // Lists to store employee and attendance records
    static List<String[]> employees = new ArrayList<>();
    static List<String[]> attendance = new ArrayList<>();

    // Map to store CSV column headers and their index
    static Map<String, Integer> columns = new HashMap<>();

    public static void main(String[] args) {

        // Step 1: Login validation
        if (!login()) {
            return; // stop program if login fails
        }

        // Step 2: Load employee information from CSV
        loadEmployees();

        // Step 3: Load attendance records from CSV
        loadAttendance();

        // Step 4: Show payroll processing menu
        payrollMenu();
    }

    // ---------------- LOGIN METHOD ----------------
    public static boolean login() {

        System.out.print("Username: ");
        String username = input.nextLine();

        System.out.print("Password: ");
        String password = input.nextLine();

        // Validate username and password
        if ((!username.equals("employee") && !username.equals("payroll_staff")) 
                || !password.equals("12345")) {

            System.out.println("Invalid username or password.");
            return false;
        }

        return true;
    }

    // ---------------- LOAD EMPLOYEE DATA ----------------
    public static void loadEmployees() {

        try (BufferedReader br = new BufferedReader(new FileReader("employee_data.csv"))) {

            // Read header row
            String headerLine = br.readLine();
            String[] headers = headerLine.split(",");

            // Store column name and its position
            for (int i = 0; i < headers.length; i++) {
                columns.put(headers[i].trim(), i);
            }

            String line;

            // Read remaining rows
            while ((line = br.readLine()) != null) {

                // Split CSV safely (handles commas inside quotes)
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                employees.add(data);
            }

        } catch (Exception e) {
            System.out.println("Error reading employee_data.csv");
        }
    }

    // ---------------- LOAD ATTENDANCE DATA ----------------
    public static void loadAttendance() {

        try (BufferedReader br = new BufferedReader(new FileReader("attendance_record.csv"))) {

            String line = br.readLine(); // skip header

            while ((line = br.readLine()) != null) {

                String[] data = line.split(",");

                attendance.add(data);
            }

        } catch (Exception e) {
            System.out.println("Error reading attendance_record.csv");
        }
    }

    // ---------------- PAYROLL MENU ----------------
    public static void payrollMenu() {

        while (true) {

            System.out.println("\n===== MotorPH Payroll Menu =====");
            System.out.println("1. Process Payroll for One Employee");
            System.out.println("2. Process Payroll for All Employees");
            System.out.println("3. Exit");

            System.out.print("Enter choice: ");
            int choice = input.nextInt();
            input.nextLine();

            if (choice == 3) {
                System.out.println("Exiting system...");
                break;
            }

            if (choice == 1) {

                // Payroll for a specific employee
                System.out.print("Enter Employee Number: ");
                String empNum = input.nextLine();

                processPayroll(empNum);

            } else if (choice == 2) {

                // Payroll for all employees
                Set<String> processed = new HashSet<>();

                for (String[] emp : employees) {

                    String empNum = emp[columns.get("Employee #")];

                    if (!processed.contains(empNum)) {

                        processPayroll(empNum);
                        processed.add(empNum);
                    }
                }
            }
        }
    }

    // ---------------- PROCESS PAYROLL ----------------
    public static void processPayroll(String empNum) {

        String name = "";
        String position = "";
        double hourlyRate = 0;

        // Find employee details from employee_data.csv
        for (String[] emp : employees) {

            if (emp[columns.get("Employee #")].equals(empNum)) {

                // Combine first and last name
                name = emp[columns.get("First Name")] + " " +
                       emp[columns.get("Last Name")];

                position = emp[columns.get("Position")];

                // Remove commas and quotes before parsing number
                String rateText = emp[columns.get("Hourly Rate")]
                        .replace(",", "")
                        .replace("\"", "")
                        .trim();

                hourlyRate = Double.parseDouble(rateText);

                break;
            }
        }

        // Compute total hours worked from attendance records
        double totalHours = 0;

        for (String[] att : attendance) {

            if (att[0].equals(empNum)) {

                double hours = computeHours(att[2], att[3]);

                totalHours += hours;
            }
        }

        // Calculate gross salary
        double gross = totalHours * hourlyRate;

        // Sample deductions
        double sss = gross * 0.0363;
        double philhealth = gross * 0.0275;
        double pagibig = 100;
        double tax = gross * 0.10;

        double deductions = sss + philhealth + pagibig + tax;

        // Compute net salary
        double netSalary = gross - deductions;

        // Display payroll result
        System.out.println("\n================================");
        System.out.println("Employee Number: " + empNum);
        System.out.println("Employee Name  : " + name);
        System.out.println("Position       : " + position);

        System.out.println("\nHours Worked: " + totalHours);

        System.out.printf("Gross Salary: %.2f\n", gross);

        System.out.println("\nDeductions");
        System.out.printf("SSS: %.2f\n", sss);
        System.out.printf("PhilHealth: %.2f\n", philhealth);
        System.out.printf("Pag-IBIG: %.2f\n", pagibig);
        System.out.printf("Tax: %.2f\n", tax);

        System.out.printf("\nNet Salary: %.2f\n", netSalary);
        System.out.println("================================");
    }

    // ---------------- COMPUTE WORK HOURS ----------------
    public static double computeHours(String timeIn, String timeOut) {

        try {

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

            Date in = sdf.parse(timeIn);
            Date out = sdf.parse(timeOut);

            // Company official work hours
            Date start = sdf.parse("08:00");
            Date end = sdf.parse("17:00");

            // Adjust if employee arrives early or leaves late
            if (in.before(start)) in = start;
            if (out.after(end)) out = end;

            long diff = out.getTime() - in.getTime();

            if (diff < 0) return 0;

            double hours = diff / (1000.0 * 60 * 60);

            // Rounding rules
            if (hours >= 7.75 && hours < 8) return 8;
            if (hours >= 7.25 && hours < 7.75) return 7.5;

            return hours;

        } catch (Exception e) {

            return 0;
        }
    }
}