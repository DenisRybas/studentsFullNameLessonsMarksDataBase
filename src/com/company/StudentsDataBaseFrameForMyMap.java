package com.company;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.*;


public class StudentsDataBaseFrameForMyMap extends JFrame {
    private JTable gradesTable;
    private JButton getGradesButton;
    private JButton loadFromFileButton;
    private JButton saveToFileButton;
    private JPanel mainPanel;
    private DefaultListModel<String> model;
    private JList<String> studentsList;
    private SimpleHashMap<String, SimpleHashMap<String, String>> studentsDataBase = null; //Map<family, lesson, mark>

    private JFrame frameMain = null;

    public StudentsDataBaseFrameForMyMap() {
        this.setTitle("Students DataBase");
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();

        gradesTable.setRowHeight(gradesTable.getRowHeight() + 20);

        loadFromFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


                JFileChooser dialog = new JFileChooser();
                if (dialog.showOpenDialog(frameMain) == JFileChooser.APPROVE_OPTION) {
                    File file = dialog.getSelectedFile();
                    if (!file.exists()) {
                        throw new RuntimeException("File not found!");
                    }
                    String cvsSplitBy = ",";

                    try (BufferedReader in = new BufferedReader(new FileReader(file.getAbsoluteFile()))) {
                        String s;
                        studentsDataBase = new SimpleHashMap();
                        model = new DefaultListModel<>();

                        while ((s = in.readLine()) != null) {
                            String[] row = s.split(cvsSplitBy);
                            String lesson = row[0];
                            String family = row[1];
                            String mark = row[2];
                            if (!studentsDataBase.containsKey(family)) {
                                studentsDataBase.put(family, new SimpleHashMap<>());
                                model.addElement(family);
                            }
                            if (!studentsDataBase.get(family).containsKey(lesson))
                                studentsDataBase.get(family).put(lesson, mark);

                        }
                        studentsList.setModel(model);

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        saveToFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser dialog = new JFileChooser();
                File file = new File("");
                if (dialog.showOpenDialog(frameMain) == JFileChooser.APPROVE_OPTION) {
                    file = dialog.getSelectedFile();
                    if (!file.exists()) {
                        throw new RuntimeException("File not found!");
                    }
                }

                BufferedWriter  writer;

                try {
                    writer = new BufferedWriter(new FileWriter(file));

                    StringBuilder sb = new StringBuilder();
                    for (String student : studentsDataBase.keySet()) {
                        for (String lesson : studentsDataBase.get(student).keySet()) {
                            String mark = studentsDataBase.get(student).get(lesson);
                            sb.append(lesson);
                            sb.append(',');
                            sb.append(student);
                            sb.append(',');
                            sb.append(mark);
                            sb.append('\n');
                        }
                    }
                    writer.write(sb.toString());
                    writer.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        studentsList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    DefaultTableModel model = new DefaultTableModel(
                            new String[]{"Lesson", "Mark"}, 0
                    );

                    String student = studentsList.getSelectedValue();

                    SimpleHashMap<String, String> lessonsAndMarks = studentsDataBase.get(student);

                    for (String lesson : lessonsAndMarks.keySet()) {
                        String mark = lessonsAndMarks.get(lesson);
                        model.addRow(new String[]{lesson, mark});
                    }
                    gradesTable.setModel(model);
                }
            }
        });


        getGradesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel model = new DefaultTableModel(
                        new String[]{"Lesson", "Mark"}, 0
                );

                String student = studentsList.getSelectedValue();

                SimpleHashMap<String, String> lessonsAndMarks = studentsDataBase.get(student);
                for (String lesson : lessonsAndMarks.keySet()) {
                    String mark = lessonsAndMarks.get(lesson);
                    model.addRow(new String[]{lesson, mark});
                }
                gradesTable.setModel(model);
            }
        });
    }


    public static void main(String[] args) throws
            ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        Locale.setDefault(Locale.ROOT);

        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        SwingUtils.setDefaultFont("Arial", 20);

        EventQueue.invokeLater(() -> {
            try {
                JFrame frameMain = new StudentsDataBaseFrameForJavaMap();
                frameMain.setVisible(true);
                frameMain.setExtendedState(MAXIMIZED_BOTH);
            } catch (Exception ex) {
                SwingUtils.showErrorMessageBox(ex);
            }
        });
    }
}

