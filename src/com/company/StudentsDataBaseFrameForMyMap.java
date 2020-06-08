package com.company;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;


public class StudentsDataBaseFrameForMyMap extends JFrame {
    private JTable gradesTable;
    private JButton getGradesButton;
    private JButton loadFromFileButton;
    private JButton saveToFileButton;
    private JPanel mainPanel;
    private DefaultListModel<String> studentsListModel = new DefaultListModel<>();
    private JList<String> studentsList;
    private JLabel studentsLabel;
    private JButton addStudentButton;
    private JButton removeStudentButton;
    private JButton addLessonButton;
    private JButton removeLessonButton;
    private JButton renameStudentButton;
    private JButton saveChangesButton;
    private SimpleHashMap<String, Map<String, String>> studentsDataBase = new SimpleHashMap<>(); //Map<family, lesson, mark>

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
                        studentsDataBase = new SimpleHashMap<>();
                        studentsListModel = new DefaultListModel<>();

                        while ((s = in.readLine()) != null) {
                            String[] row = s.split(cvsSplitBy);
                            String lesson = row[0];
                            String family = row[1];
                            String mark = row[2];
                            if (!studentsDataBase.containsKey(family)) {
                                studentsDataBase.put(family, new SimpleHashMap<>());
                                studentsListModel.addElement(family);
                            }
                            if (!studentsDataBase.get(family).containsKey(lesson))
                                studentsDataBase.get(family).put(lesson, mark);

                        }
                        studentsList.setModel(studentsListModel);

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
                if (dialog.showOpenDialog(frameMain) == JFileChooser.APPROVE_OPTION) {
                    File file = dialog.getSelectedFile();
                    if (!file.exists()) {
                        throw new RuntimeException("File not found!");
                    }

                    BufferedWriter writer;

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
            }
        });

        studentsList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    gradesTable.setModel(displayLessonsAndMarksOfStud(studentsList.getSelectedValue()));
                }

            }
        });


        getGradesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gradesTable.setModel(displayLessonsAndMarksOfStud(studentsList.getSelectedValue()));
            }
        });

        addStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    boolean fl = false;
                    int i = 1;
                    do {
                        String key = "новый студент " + i;
                        if (!studentsDataBase.containsKey(key)) {
                            studentsDataBase.put(key, new SimpleHashMap<>());
                            studentsListModel.addElement(key);
                            studentsList.setModel(studentsListModel);
                            fl = true;
                        }
                        i++;
                    } while (!fl);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        removeStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    studentsDataBase.remove(studentsList.getSelectedValue());
                    ((DefaultListModel<String>) studentsList.getModel()).remove(((DefaultListModel<String>) studentsList.getModel()).indexOf(studentsList.getSelectedValue()));
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                }
            }
        });

        addLessonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!studentsListModel.isEmpty()) {
                    //saveChangesButton.doClick();
                    DefaultTableModel model = (DefaultTableModel) gradesTable.getModel();
                    boolean fl = false;
                    int i = 1;
                    Map<String, String> lessonAndMark = studentsDataBase.get(studentsList.getSelectedValue());
                    do {
                        String lesson = "Новый предмет " + i;
                        if (!lessonAndMark.containsKey(lesson)) {
                            model.addRow(new String[]{lesson, "Оценка"});
                            lessonAndMark.put(lesson, "Оценка");
                            studentsDataBase.put(studentsList.getSelectedValue(), lessonAndMark);
                            fl = true;
                        }
                        i++;
                    } while (!fl);
                    gradesTable.setModel(model);
                    gradesTable.revalidate();
                }
            }
        });

        removeLessonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel model = displayLessonsAndMarksOfStud(studentsList.getSelectedValue());
                studentsDataBase.get(studentsList.getSelectedValue()).remove(gradesTable.getValueAt(gradesTable.getSelectedRow(), 0).toString());
                model.removeRow(gradesTable.getSelectedRow());
                gradesTable.setModel(model);
                gradesTable.revalidate();
            }
        });

        renameStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ListSelectionModel selectionModel = studentsList.getSelectionModel();
                int index = selectionModel.getMinSelectionIndex();
                if (index == -1) {
                    return;
                }

                Object item = studentsListModel.getElementAt(index);
                String text = JOptionPane.showInputDialog("Rename item", item);
                String newItem;

                if (text != null) {
                    newItem = text.trim();
                } else {
                    return;
                }

                if (!newItem.isEmpty()) {
                    Map<String, String> lessonsAndMarks = studentsDataBase.get(studentsList.getModel().getElementAt(index));
                    studentsDataBase.remove(studentsList.getModel().getElementAt(index));
                    studentsListModel.remove(index);
                    studentsListModel.add(index, newItem);
                    studentsDataBase.put(studentsList.getModel().getElementAt(index), lessonsAndMarks);
                }

            }
        });


        saveChangesButton.addActionListener(new ActionListener() {
            @Override//создать новый словарь
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel model = (DefaultTableModel) gradesTable.getModel();
                String[] modelData = new String[model.getRowCount()];
                if (studentsDataBase.containsKey(studentsList.getSelectedValue())) {
                    studentsDataBase.get(studentsList.getSelectedValue()).clear();
                    for (int i = 0; i < model.getRowCount(); i++) {
                        modelData[i] = (model.getValueAt(i, 0).toString());
                        String key = modelData[i];
                        studentsDataBase.get(studentsList.getSelectedValue()).put(key, model.getValueAt(i, 1).toString());
                    }
                    gradesTable.setModel(displayLessonsAndMarksOfStud(studentsList.getSelectedValue()));

                    gradesTable.revalidate();
                }
            }
        });

        gradesTable.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    saveChangesButton.doClick();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        gradesTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                gradesTable.setValueAt(gradesTable.getModel().getValueAt(e.getFirstRow(), e.getColumn()), e.getFirstRow(), e.getColumn());
                saveChangesButton.doClick();
            }
        });
    }

    private DefaultTableModel displayLessonsAndMarksOfStud(String student) {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Lesson", "Mark"}, 0
        );

        Map<String, String> lessonsAndMarks = studentsDataBase.get(student);
        try {
            for (String lesson : lessonsAndMarks.keySet()) {
                String mark = lessonsAndMarks.get(lesson);
                model.addRow(new String[]{lesson, mark});
            }
        } catch (NullPointerException ex) {
            SwingUtils.showErrorMessageBox(ex);
        }
        return model;
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

