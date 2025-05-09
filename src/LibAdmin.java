import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.awt.*;

import javax.swing.JTable;
import java.io.FileOutputStream;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author Eriann
 */
public class LibAdmin extends javax.swing.JFrame {

    /**
     * Creates new form LibAdmin
     */
    
    Connection con;
    PreparedStatement pst;
    ResultSet rs;
    
    public LibAdmin() {
        initComponents();
        loadUsersToTable();
        loadBooks();
        showPieChart();
        loadStudentss();
        countTotalBooks();
        deleteBook();
        loadBookss();
        loadtimer();
        countTotalStudent();
        loadStudentsss();
        deleteStudent();
        loadtstudenttime();
        loadInactiveStudents();
        Viewrecords();
        studentdetails();
        showDate();
        showTime();
        showOverdueBooks();
    }
    
     public void refreshdata(){
        loadUsersToTable();
        loadBooks();
        loadBookss();
        loadtimer();
        countTotalStudent();
        loadStudentsss();
        deleteStudent();
        loadtstudenttime();
        loadInactiveStudents();
        Viewrecords();
        studentdetails();
        showOverdueBooks();
    }
     //notif



     
     
     
     //time
      public void showTime(){
        new javax.swing.Timer(0, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                Date d = new Date();
                SimpleDateFormat s = new SimpleDateFormat("hh-mm-ss");
                String tim = s.format(d);
                time.setText(tim);
            }
        }).start();
    }

      //overdue
      public void showOverdueBooks() {
            DefaultTableModel model = (DefaultTableModel) trecordsss.getModel();
            model.setRowCount(0); // Clear existing rows

            try {
                Connection con = LibManSys.getConnection();
                String sql = "SELECT * FROM issue_book_details WHERE status = 'Borrowed'";
                PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery();

                Date currentDate = new Date();

                while (rs.next()) {
                    Date dueDate = rs.getDate("due_date");

                    if (currentDate.after(dueDate)) {
                        String id = rs.getString("id");
                        String isbn = rs.getString("isbn");
                        String title = rs.getString("title");
                        String lrn = rs.getString("lrn");
                        String studentName = rs.getString("student_name");
                        String issueDate = rs.getString("issue_date");
                        String due = rs.getString("due_date");
                        String status = "Overdue";

                        Object[] row = {id, isbn, title, lrn, studentName, issueDate, "", due, status};
                        model.addRow(row);
                    }
                }

                rs.close();
                pst.close();
                con.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
     
     //date
     public void showDate(){
        Date d = new Date();
        SimpleDateFormat s = new SimpleDateFormat("dd-MM-yyyy");
        String dat = s.format(d);
        date.setText(dat);
}

     
     public void studentdetails() {
    DefaultTableModel model = (DefaultTableModel) STUDENT.getModel();
    model.setRowCount(0);  

    String query = "SELECT * FROM student ORDER BY student_id DESC"; 

    try (
        Connection con = LibManSys.getConnection();  
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query)) {

        while (rs.next()) {
            String id = rs.getString("student_id");
            String isbn = rs.getString("lrn");
            String title = rs.getString("fullname");
            String lrn = rs.getString("grade_lvl");
            String studentname = rs.getString("section");
            String issuedate = rs.getString("contact_number");
            

            Object[] row = {id, isbn, title, lrn, studentname, issuedate,};
            model = (DefaultTableModel) STUDENT.getModel();
            model.addRow(row);
            
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
     
     
     public void exportToPDF(JTable table) {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Save PDF Report");
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setSelectedFile(new File("BorrowedBooksReport.pdf"));

    int userSelection = fileChooser.showSaveDialog(null);

    if (userSelection == JFileChooser.APPROVE_OPTION) {
        File fileToSave = fileChooser.getSelectedFile();

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
            document.open();

            // Title
            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Borrowed Books Report", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" ")); // spacing

            // Full Records Table
            PdfPTable pdfTable = new PdfPTable(table.getColumnCount());
            pdfTable.setWidthPercentage(100);

            // Add headers
            for (int i = 0; i < table.getColumnCount(); i++) {
                pdfTable.addCell(new PdfPCell(new Phrase(table.getColumnName(i))));
            }

            // For borrower summary
            Map<String, Integer> borrowerCount = new HashMap<>();

            // For book summary
            Map<String, Integer> totalBorrowedPerBook = new HashMap<>();
            Map<String, Set<String>> uniqueBorrowersPerBook = new HashMap<>();

            // Add table rows
            for (int row = 0; row < table.getRowCount(); row++) {
                for (int col = 0; col < table.getColumnCount(); col++) {
                    Object value = table.getValueAt(row, col);
                    pdfTable.addCell(value != null ? value.toString() : "");
                }

                String bookTitle = table.getValueAt(row, 2).toString(); // or use index 1 for ISBN
                String studentName = table.getValueAt(row, 4).toString();

                // Count total borrowed per book
                totalBorrowedPerBook.put(bookTitle, totalBorrowedPerBook.getOrDefault(bookTitle, 0) + 1);

                // Track unique borrowers per book
                uniqueBorrowersPerBook.putIfAbsent(bookTitle, new HashSet<>());
                uniqueBorrowersPerBook.get(bookTitle).add(studentName);

                // Count most frequent borrower
                borrowerCount.put(studentName, borrowerCount.getOrDefault(studentName, 0) + 1);
            }

            document.add(pdfTable);
            document.add(new Paragraph(" "));

            // Top Borrower Section
            String topBorrower = "";
            int maxBorrowed = 0;
            for (Map.Entry<String, Integer> entry : borrowerCount.entrySet()) {
                if (entry.getValue() > maxBorrowed) {
                    maxBorrowed = entry.getValue();
                    topBorrower = entry.getKey();
                }
            }

            Font summaryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Paragraph summary = new Paragraph("Top Borrower: " + topBorrower + " (" + maxBorrowed + " books borrowed)", summaryFont);
            summary.setAlignment(Element.ALIGN_LEFT);
            document.add(summary);

            document.add(new Paragraph(" "));

            // Summary Table (per book)
            Paragraph subTitle = new Paragraph("Summary by Book Title", fontTitle);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subTitle);
            document.add(new Paragraph(" "));

            PdfPTable summaryTable = new PdfPTable(3);
            summaryTable.setWidthPercentage(100);
            summaryTable.addCell("Book Title");
            summaryTable.addCell("Total Times Borrowed");
            summaryTable.addCell("Unique Borrowers");

            for (String book : totalBorrowedPerBook.keySet()) {
                summaryTable.addCell(book);
                summaryTable.addCell(String.valueOf(totalBorrowedPerBook.get(book)));
                summaryTable.addCell(String.valueOf(uniqueBorrowersPerBook.get(book).size()));
            }

            document.add(summaryTable);

            document.close();
            JOptionPane.showMessageDialog(null, "PDF generated successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error generating PDF: " + e.getMessage());
        }
    }
}

     
     public void Viewrecords() {
    DefaultTableModel model = (DefaultTableModel) trecords.getModel();
    model.setRowCount(0);  // Clear table rows

    String query = "SELECT * FROM issue_book_details ORDER BY id DESC"; 

    try (
        Connection con = LibManSys.getConnection();  
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query)) {

        while (rs.next()) {
            String id = rs.getString("id");
            String isbn = rs.getString("isbn");
            String title = rs.getString("title");
            String lrn = rs.getString("lrn");
            String studentname = rs.getString("student_name");
            String issuedate = rs.getString("issue_date");
            String duedate = rs.getString("due_date");
            String status = rs.getString("status");

            Object[] row = {id, isbn, title, lrn, studentname, issuedate, duedate, status};
            model = (DefaultTableModel) trecords.getModel();
            model.addRow(row);
            
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    public void clearTable() {
    DefaultTableModel model = (DefaultTableModel) trecords.getModel();
    model.setRowCount(0);
}

    public void searchh(){
        Date ufromDate = ISSUEDATE.getDatoFecha();
        Date utoDate = DUEDATES.getDatoFecha();

        long l1 = ufromDate.getTime();
        long l2 = utoDate.getTime();

        java.sql.Date fromDate = new java.sql.Date(l1);
        java.sql.Date toDate = new java.sql.Date(l2);

        try {
            Connection con = LibManSys.getConnection();
            String sql = "SELECT * FROM issue_book_details WHERE issue_date BETWEEN ? AND ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setDate(1, fromDate);
            pst.setDate(2, toDate);

            ResultSet rs = pst.executeQuery();

            
            DefaultTableModel model = (DefaultTableModel) trecords.getModel();
            model.setRowCount(0); 

            while (rs.next()) {
                String id = rs.getString("id");
                String isbn = rs.getString("isbn");
                String title = rs.getString("title");
                String lrn = rs.getString("lrn");
                String studentname = rs.getString("student_name");
                String issuedate = rs.getString("issue_date");
                String duedate = rs.getString("due_date");
                String status = rs.getString("status");

                Object[] row = {id, isbn, title, lrn, studentname, issuedate, duedate, status};
                model.addRow(row);
            }

        } catch (Exception e) {
            e.printStackTrace(); 
        }

            }
    
   


    
    
    //NO OF BOOKS
       public void countTotalBooks() {
            try (Connection con = LibManSys.getConnection();
                 PreparedStatement pst = con.prepareStatement("SELECT COUNT(*) AS total FROM books");
                 ResultSet rs = pst.executeQuery()) {

                if (rs.next()) {
                    int total = rs.getInt("total");
                    nobooks.setText(String.valueOf(total));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void deleteBook() {
            countTotalBooks();
        }

        public void loadBookss() {
            countTotalBooks();
        }

        public void loadtimer() {
            countTotalBooks();
            javax.swing.Timer timer = new javax.swing.Timer(5000, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    countTotalBooks();
                }
            });
            timer.start();
        }
        //STUDENT NO
          public void countTotalStudent() {
        Connection con = LibManSys.getConnection();
        String sql = "SELECT COUNT(*) AS total FROM student"; 

                try {
                    PreparedStatement pst = con.prepareStatement(sql);
                    ResultSet rs = pst.executeQuery();

                    if (rs.next()) {
                        int total = rs.getInt("total");
                        nostudents.setText(String.valueOf(total));
                    }

                    rs.close();
                    pst.close();
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
                public void deleteStudent() {



                countTotalBooks(); 
            }
                public void loadStudentsss() {


            countTotalBooks(); 
        }
                public void loadtstudenttime() {
            countTotalBooks(); 

            javax.swing.Timer timer = new javax.swing.Timer(5000, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    countTotalBooks();
                }
            });
            timer.start();
            
        }
                
                






   
    public void showPieChart(){
        
        
      DefaultPieDataset barDataset = new DefaultPieDataset();
    try {
        Connection con = LibManSys.getConnection();
        String sql = "select title, count(*) as issue_count from issue_book_details group by title";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            barDataset.setValue(rs.getString("title"), new Double(rs.getDouble("issue_count")));
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
      
      
       JFreeChart piechart = ChartFactory.createPieChart("ISSUE BOOKS",barDataset, false,true,false);//explain
      
        PiePlot piePlot =(PiePlot) piechart.getPlot();
      
      
       piePlot.setSectionPaint("IPhone 5s", new Color(255,255,102));
        piePlot.setSectionPaint("SamSung Grand", new Color(102,255,102));
        piePlot.setSectionPaint("MotoG", new Color(255,102,153));
        piePlot.setSectionPaint("Nokia Lumia", new Color(0,204,204));
      
       
        piePlot.setBackgroundPaint(Color.white);
        
        
        ChartPanel barChartPanel = new ChartPanel(piechart);
        pPie.removeAll();
        pPie.add(barChartPanel, BorderLayout.CENTER);
        pPie.validate();
    }
    
    private void fetchUserData(int userId) {
    String query = "SELECT * FROM accounts WHERE ID = ?";

    try {
            con = LibManSys.getConnection();
            PreparedStatement pst = con.prepareStatement(query);

            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                textUser.setText(rs.getString("Username"));
                textPass.setText(rs.getString("Password"));
                jComboRole.setSelectedItem(rs.getString("Role"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //USER
    public void loadUsersToTable() {
        DefaultTableModel model = (DefaultTableModel) tableUser.getModel();
        model.setRowCount(0); 

        String query = "SELECT ID, Username, Password, Role FROM accounts ORDER BY ID DESC"; 

        try (
            Connection con = LibManSys.getConnection();  
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("ID"),
                    rs.getString("Username"),
                    rs.getString("Password"),
                    rs.getString("Role"),
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //BOOKS
    public void loadBooks() {
        DefaultTableModel model = (DefaultTableModel) tbooks.getModel();
        model.setRowCount(0); 

        String query = "SELECT book_id, title, author, isbn, category, quantity, published_date FROM books ORDER BY book_id DESC"; 

        try (
            Connection con = LibManSys.getConnection();  
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("isbn"),
                    rs.getString("category"),
                    rs.getInt("quantity"),
                    rs.getString("published_date"),
                    
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
            private void fetchBookData(int bookId) {
         String query = "SELECT title, author, isbn, category, quantity, published_date FROM books WHERE book_id = ?";

         try (Connection con = LibManSys.getConnection();
              PreparedStatement pst = con.prepareStatement(query)) {

             pst.setInt(1, bookId);  

             try (ResultSet rs = pst.executeQuery()) {
                 if (rs.next()) {
                     txttitle.setText(rs.getString("title"));
                     txtauthor.setText(rs.getString("author"));
                     txtisbn.setText(rs.getString("isbn"));
                     txtcategory.setText(rs.getString("category"));
                     txtquantity.setText(String.valueOf(rs.getInt("quantity")));  

                     
                     java.sql.Date sqlDate = rs.getDate("published_date");
                     if (sqlDate != null) {
                         txtpublish.setDate(new java.util.Date(sqlDate.getTime()));
                     } else {
                         txtpublish.setDate(null);
                     }

                 } else {
                     JOptionPane.showMessageDialog(null, "No book found with this ID.");
                 }
             }

         } catch (SQLException e) {
             e.printStackTrace();
             JOptionPane.showMessageDialog(null, "Error fetching book data: " + e.getMessage());
         }
     }
            
    public void loadBorrowed() {
        DefaultTableModel model = (DefaultTableModel) tstudent.getModel();
        model.setRowCount(0); 

        String query = "SELECT student_id, lrn, fullname, grade_lvl, section, contact_number, remarks FROM student ORDER BY student_id DESC"; 

        try (
            Connection con = LibManSys.getConnection();  
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Object[] row = {
                    rs.getString("student_id"),
                    rs.getString("lrn"),
                    rs.getString("fullname"),
                    rs.getString("grade_lvl"),
                    rs.getString("section"),
                    rs.getString("contact_number"),
                    rs.getString("remarks")
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

       //MANAGE STUDENT
       public void loadStudentss() {
        DefaultTableModel model = (DefaultTableModel) tstudent.getModel();
        model.setRowCount(0); 

        String query = "SELECT student_id, lrn, fullname, grade_lvl, section, contact_number, remarks FROM student ORDER BY student_id DESC"; 

        try (
            Connection con = LibManSys.getConnection();  
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Object[] row = {
                    rs.getString("student_id"),
                    rs.getString("lrn"),
                    rs.getString("fullname"),
                    rs.getString("grade_lvl"),
                    rs.getString("section"),
                    rs.getString("contact_number"),
                    rs.getString("remarks")
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
       private void fetchStudentData(long lrn) {
            String query = "SELECT lrn, fullname, grade_lvl, section, contact_number  FROM student WHERE student_id = ?";

            try (Connection con = LibManSys.getConnection();
                 PreparedStatement pst = con.prepareStatement(query)) {

                pst.setLong(1, lrn);  

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        txtlrn.setText(rs.getString("lrn"));
                        txtfname.setText(rs.getString("fullname"));
                        txtgrade.setText(rs.getString("grade_lvl"));
                        txtsection.setText(rs.getString("section"));
                        txtcontact.setText(rs.getString("contact_number"));
                    } else {
                        JOptionPane.showMessageDialog(null, "No Student found with this ID.");
                    }
                
                }

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error fetching student data: " + e.getMessage());
            }
        }
       
       //ARCHIVE 
       public void loadInactiveStudents() {
    Connection con = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    DefaultTableModel model = (DefaultTableModel) tarchive.getModel();
    model.setRowCount(0); // clear previous data

    try {
        con = LibManSys.getConnection(); // your connection method
        String query = "SELECT student_id, lrn, fullname, grade_lvl, section, contact_number, remarks, timestamp FROM inactive_students";
        pst = con.prepareStatement(query);
        rs = pst.executeQuery();

        while (rs.next()) {
            int studentId = rs.getInt("student_id");
            String lrn = rs.getString("lrn");
            String fullname = rs.getString("fullname");
            String gradeLvl = rs.getString("grade_lvl");
            String section = rs.getString("section");
            String contact = rs.getString("contact_number");
            String remarks = rs.getString("remarks");
            String date = rs.getString("timestamp");

            model.addRow(new Object[]{studentId, lrn, fullname, gradeLvl, section, contact, remarks, date});
        }

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Failed to load archived students.");
    } finally {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
            if (con != null) con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

           






    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jManageUser = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel35 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        iDashboard = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        nobooks = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        date = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        nostudents = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        time = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        STUDENT = new rojerusan.RSTableMetro();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        pPie = new javax.swing.JPanel();
        iManageUSer = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableUser = new javax.swing.JTable();
        jLabel10 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        textUser = new javax.swing.JTextField();
        textPass = new javax.swing.JTextField();
        jComboRole = new javax.swing.JComboBox<>();
        jLabel22 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        iManageBooks = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbooks = new javax.swing.JTable();
        jLabel11 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        txtbook = new javax.swing.JTextField();
        txttitle = new javax.swing.JTextField();
        txtauthor = new javax.swing.JTextField();
        txtisbn = new javax.swing.JTextField();
        txtquantity = new javax.swing.JTextField();
        txtcategory = new javax.swing.JTextField();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jLabel34 = new javax.swing.JLabel();
        txtpublish = new com.toedter.calendar.JDateChooser();
        searchh = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        iManageStudents = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tstudent = new javax.swing.JTable();
        jLabel15 = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        txtlrn = new javax.swing.JTextField();
        txtfname = new javax.swing.JTextField();
        txtgrade = new javax.swing.JTextField();
        txtsection = new javax.swing.JTextField();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        archivebtn = new javax.swing.JButton();
        jLabel31 = new javax.swing.JLabel();
        search2 = new javax.swing.JTextField();
        jLabel51 = new javax.swing.JLabel();
        txtcontact = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        ReturnBooks = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel57 = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        jPanel14 = new javax.swing.JPanel();
        DUEDATES = new rojeru_san.componentes.RSDateChooser();
        ISSUEDATE = new rojeru_san.componentes.RSDateChooser();
        jLabel28 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        SEARCHH = new javax.swing.JButton();
        jScrollPane7 = new javax.swing.JScrollPane();
        trecords = new javax.swing.JTable();
        jButton7 = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jScrollPane9 = new javax.swing.JScrollPane();
        trecordsss = new javax.swing.JTable();
        notif = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tarchive = new javax.swing.JTable();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        search3 = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();

        jLabel1.setText("ADMIN");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setPreferredSize(new java.awt.Dimension(1980, 150));

        jPanel2.setBackground(new java.awt.Color(54, 101, 145));
        jPanel2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 2, true));
        jPanel2.setPreferredSize(new java.awt.Dimension(1980, 150));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 50)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/header logo.png"))); // NOI18N
        jLabel3.setText("  SAUYO HIGH SCHOOL");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(2, 8, 1865, 114));

        jLabel29.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(255, 255, 255));
        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel29.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel29.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel29MouseClicked(evt);
            }
        });
        jPanel2.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(1860, 20, 30, 30));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 1920, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 60, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 130));

        jPanel3.setBackground(new java.awt.Color(54, 101, 145));
        jPanel3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/ADMIN LOGO.png"))); // NOI18N
        jLabel2.setText("ADMIN");
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/DASHBOARD LOGO.png"))); // NOI18N
        jLabel4.setText(" Dashboard");
        jLabel4.setToolTipText("");
        jLabel4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel4.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel4.setIconTextGap(10);
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel4MouseClicked(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/LOGOUT LOGO.png"))); // NOI18N
        jLabel5.setText("Logout");
        jLabel5.setToolTipText("");
        jLabel5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel5.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel5.setIconTextGap(6);
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel5MouseClicked(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/add book.png"))); // NOI18N
        jLabel6.setText("   Manage Books");
        jLabel6.setToolTipText("");
        jLabel6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel6.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel6.setIconTextGap(1);
        jLabel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel6MouseClicked(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/RECORD.png"))); // NOI18N
        jLabel7.setText("View All Records");
        jLabel7.setToolTipText("");
        jLabel7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel7.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel7.setIconTextGap(8);
        jLabel7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel7MouseClicked(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/ovedue.png"))); // NOI18N
        jLabel8.setText("Overdue Books");
        jLabel8.setToolTipText("");
        jLabel8.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel8.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel8.setIconTextGap(18);
        jLabel8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel8MouseClicked(evt);
            }
        });

        jManageUser.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jManageUser.setForeground(new java.awt.Color(255, 255, 255));
        jManageUser.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jManageUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/ADD AMIN.png"))); // NOI18N
        jManageUser.setText("Manage User");
        jManageUser.setToolTipText("");
        jManageUser.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jManageUser.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jManageUser.setIconTextGap(10);
        jManageUser.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jManageUserMouseClicked(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/PERSON (2).png"))); // NOI18N
        jLabel17.setText("  Manage Students");
        jLabel17.setToolTipText("");
        jLabel17.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel17.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel17.setIconTextGap(1);
        jLabel17.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel17MouseClicked(evt);
            }
        });

        jSeparator1.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator1.setForeground(new java.awt.Color(255, 255, 255));
        jSeparator1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));
        jSeparator1.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N

        jLabel35.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(255, 255, 255));
        jLabel35.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel35.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/archivee.png"))); // NOI18N
        jLabel35.setText("Archive Students");
        jLabel35.setToolTipText("");
        jLabel35.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel35.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel35.setIconTextGap(18);
        jLabel35.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel35MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jManageUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 18, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jManageUser, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 278, Short.MAX_VALUE)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
        );

        getContentPane().add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 130, 270, 950));

        jTabbedPane1.setBackground(new java.awt.Color(54, 101, 145));
        jTabbedPane1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 2, true));

        iDashboard.setBackground(new java.awt.Color(54, 101, 145));
        iDashboard.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));
        iDashboard.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel4.setBackground(new java.awt.Color(54, 101, 145));
        jPanel4.setBorder(javax.swing.BorderFactory.createMatteBorder(15, 3, 3, 3, new java.awt.Color(255, 255, 255)));
        jPanel4.setForeground(new java.awt.Color(255, 255, 255));
        jPanel4.setPreferredSize(new java.awt.Dimension(260, 140));

        nobooks.setFont(new java.awt.Font("Segoe UI", 1, 50)); // NOI18N
        nobooks.setForeground(new java.awt.Color(255, 255, 255));
        nobooks.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        nobooks.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Dashboard/Bookshelf.png"))); // NOI18N
        nobooks.setText("10");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(nobooks, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(nobooks)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        iDashboard.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(106, 86, -1, -1));

        jLabel30.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(255, 255, 255));
        jLabel30.setText("TIME NOW");
        iDashboard.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(1230, 60, -1, -1));

        jPanel5.setBackground(new java.awt.Color(54, 101, 145));
        jPanel5.setBorder(javax.swing.BorderFactory.createMatteBorder(15, 3, 3, 3, new java.awt.Color(255, 255, 255)));
        jPanel5.setForeground(new java.awt.Color(255, 255, 255));
        jPanel5.setPreferredSize(new java.awt.Dimension(260, 140));

        date.setFont(new java.awt.Font("Segoe UI", 1, 38)); // NOI18N
        date.setForeground(new java.awt.Color(255, 255, 255));
        date.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        date.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/DATE.png"))); // NOI18N
        date.setText("10");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(date, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(date)
                .addContainerGap(44, Short.MAX_VALUE))
        );

        iDashboard.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(845, 86, 290, -1));

        jPanel12.setBackground(new java.awt.Color(54, 101, 145));
        jPanel12.setBorder(javax.swing.BorderFactory.createMatteBorder(15, 3, 3, 3, new java.awt.Color(255, 255, 255)));
        jPanel12.setForeground(new java.awt.Color(255, 255, 255));
        jPanel12.setPreferredSize(new java.awt.Dimension(260, 140));

        nostudents.setFont(new java.awt.Font("Segoe UI", 1, 50)); // NOI18N
        nostudents.setForeground(new java.awt.Color(255, 255, 255));
        nostudents.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        nostudents.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Dashboard/PEople.png"))); // NOI18N
        nostudents.setText("10");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(nostudents, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(nostudents)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        iDashboard.add(jPanel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(471, 86, -1, -1));

        jLabel36.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel36.setForeground(new java.awt.Color(255, 255, 255));
        jLabel36.setText("No Of Students");
        iDashboard.add(jLabel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(471, 55, -1, -1));

        jPanel13.setBackground(new java.awt.Color(54, 101, 145));
        jPanel13.setBorder(javax.swing.BorderFactory.createMatteBorder(15, 3, 3, 3, new java.awt.Color(255, 255, 255)));
        jPanel13.setForeground(new java.awt.Color(255, 255, 255));
        jPanel13.setPreferredSize(new java.awt.Dimension(260, 140));

        time.setFont(new java.awt.Font("Segoe UI", 1, 38)); // NOI18N
        time.setForeground(new java.awt.Color(255, 255, 255));
        time.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        time.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/TIME.png"))); // NOI18N
        time.setText("10");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(time, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(time)
                .addContainerGap(44, Short.MAX_VALUE))
        );

        iDashboard.add(jPanel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(1233, 86, 290, -1));

        jLabel38.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel38.setForeground(new java.awt.Color(255, 255, 255));
        jLabel38.setText("DATE");
        iDashboard.add(jLabel38, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 60, -1, -1));

        STUDENT.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 0));
        STUDENT.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Student Id", "LRN", "Fullname", "Grade Lvl", "Section", "Contact"
            }
        ));
        STUDENT.setColorBackgoundHead(new java.awt.Color(54, 101, 145));
        STUDENT.setColorBordeFilas(new java.awt.Color(255, 255, 255));
        STUDENT.setColorBordeHead(new java.awt.Color(255, 255, 255));
        STUDENT.setColorFilasBackgound1(new java.awt.Color(54, 101, 145));
        STUDENT.setColorFilasBackgound2(new java.awt.Color(54, 101, 145));
        STUDENT.setColorFilasForeground1(new java.awt.Color(255, 255, 255));
        STUDENT.setColorFilasForeground2(new java.awt.Color(255, 255, 255));
        STUDENT.setColorSelBackgound(new java.awt.Color(54, 101, 145));
        STUDENT.setFuenteHead(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        STUDENT.setRowHeight(40);
        jScrollPane3.setViewportView(STUDENT);

        iDashboard.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 400, 1013, 450));

        jLabel39.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel39.setForeground(new java.awt.Color(255, 255, 255));
        jLabel39.setText("No Of Books");
        iDashboard.add(jLabel39, new org.netbeans.lib.awtextra.AbsoluteConstraints(106, 55, -1, -1));

        jLabel40.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel40.setForeground(new java.awt.Color(255, 255, 255));
        jLabel40.setText("Student Details");
        iDashboard.add(jLabel40, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 360, -1, -1));

        pPie.setPreferredSize(new java.awt.Dimension(540, 450));
        pPie.setLayout(new java.awt.BorderLayout());
        iDashboard.add(pPie, new org.netbeans.lib.awtextra.AbsoluteConstraints(1080, 400, -1, -1));

        jTabbedPane1.addTab("tab1", iDashboard);

        iManageUSer.setBackground(new java.awt.Color(54, 101, 145));
        iManageUSer.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));

        tableUser.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "id", "Username", "Password", "Role"
            }
        ));
        tableUser.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableUserMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tableUser);

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("Username:");

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(255, 255, 255));
        jLabel19.setText("Password:");

        textPass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textPassActionPerformed(evt);
            }
        });

        jComboRole.setBackground(new java.awt.Color(54, 101, 145));
        jComboRole.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jComboRole.setForeground(new java.awt.Color(255, 255, 255));
        jComboRole.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ADMIN", "STAFF" }));
        jComboRole.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 2, true));
        jComboRole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboRoleActionPerformed(evt);
            }
        });

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(255, 255, 255));
        jLabel22.setText("Role:");

        jButton1.setBackground(new java.awt.Color(54, 101, 145));
        jButton1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("INSERT");
        jButton1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(54, 101, 145));
        jButton2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("UPDATE");
        jButton2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout iManageUSerLayout = new javax.swing.GroupLayout(iManageUSer);
        iManageUSer.setLayout(iManageUSerLayout);
        iManageUSerLayout.setHorizontalGroup(
            iManageUSerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(iManageUSerLayout.createSequentialGroup()
                .addGroup(iManageUSerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(iManageUSerLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2))
                    .addGroup(iManageUSerLayout.createSequentialGroup()
                        .addGroup(iManageUSerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(iManageUSerLayout.createSequentialGroup()
                                .addGroup(iManageUSerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(iManageUSerLayout.createSequentialGroup()
                                        .addGap(542, 542, 542)
                                        .addGroup(iManageUSerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel19)
                                            .addComponent(jLabel10)))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, iManageUSerLayout.createSequentialGroup()
                                        .addGap(48, 48, 48)
                                        .addComponent(jLabel22)))
                                .addGap(18, 18, 18)
                                .addGroup(iManageUSerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jComboRole, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(iManageUSerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(textPass, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                                        .addComponent(textUser))))
                            .addGroup(iManageUSerLayout.createSequentialGroup()
                                .addGap(617, 617, 617)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(48, 48, 48)
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 769, Short.MAX_VALUE)))
                .addContainerGap())
        );
        iManageUSerLayout.setVerticalGroup(
            iManageUSerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, iManageUSerLayout.createSequentialGroup()
                .addGap(82, 82, 82)
                .addGroup(iManageUSerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(textUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(iManageUSerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(textPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(iManageUSerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboRole, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22))
                .addGap(59, 59, 59)
                .addGroup(iManageUSerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 113, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 511, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("tab2", iManageUSer);

        iManageBooks.setBackground(new java.awt.Color(54, 101, 145));
        iManageBooks.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));

        jScrollPane1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        tbooks.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));
        tbooks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Book ID", "Title", "Author", "Isbn", "Category", "Quantity", "Published Date"
            }
        ));
        tbooks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbooksMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tbooks);

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 50)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("MANAGE BOOKS");

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setText("BOOK ID :");

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(255, 255, 255));
        jLabel23.setText("TITLE:");

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(255, 255, 255));
        jLabel24.setText("AUTHOR:");

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(255, 255, 255));
        jLabel25.setText("ISBN:");

        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(255, 255, 255));
        jLabel26.setText("CATEGORY:");

        jLabel27.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(255, 255, 255));
        jLabel27.setText("QUANTITY:");

        txtbook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtbookActionPerformed(evt);
            }
        });

        txtauthor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtauthorActionPerformed(evt);
            }
        });

        txtisbn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtisbnActionPerformed(evt);
            }
        });

        txtquantity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtquantityActionPerformed(evt);
            }
        });

        txtcategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtcategoryActionPerformed(evt);
            }
        });

        jButton4.setBackground(new java.awt.Color(54, 101, 145));
        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton4.setForeground(new java.awt.Color(255, 255, 255));
        jButton4.setText("ADD");
        jButton4.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setBackground(new java.awt.Color(54, 101, 145));
        jButton5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton5.setForeground(new java.awt.Color(255, 255, 255));
        jButton5.setText("UPDATE");
        jButton5.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setBackground(new java.awt.Color(54, 101, 145));
        jButton6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton6.setForeground(new java.awt.Color(255, 255, 255));
        jButton6.setText("DELETE");
        jButton6.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jLabel34.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel34.setForeground(new java.awt.Color(255, 255, 255));
        jLabel34.setText("PUBLISHED DATE:");

        searchh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchhActionPerformed(evt);
            }
        });
        searchh.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                searchhKeyTyped(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/searchbutton.png"))); // NOI18N
        jLabel12.setText("SEARCH");

        javax.swing.GroupLayout iManageBooksLayout = new javax.swing.GroupLayout(iManageBooks);
        iManageBooks.setLayout(iManageBooksLayout);
        iManageBooksLayout.setHorizontalGroup(
            iManageBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(iManageBooksLayout.createSequentialGroup()
                .addGroup(iManageBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(iManageBooksLayout.createSequentialGroup()
                        .addGroup(iManageBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(iManageBooksLayout.createSequentialGroup()
                                .addGroup(iManageBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(iManageBooksLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txttitle, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(iManageBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, iManageBooksLayout.createSequentialGroup()
                                            .addContainerGap()
                                            .addComponent(jLabel24)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(txtauthor, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(iManageBooksLayout.createSequentialGroup()
                                            .addGap(297, 297, 297)
                                            .addComponent(jLabel16)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(txtbook, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(122, 122, 122)
                                .addGroup(iManageBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(iManageBooksLayout.createSequentialGroup()
                                        .addComponent(jLabel27)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtquantity, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(iManageBooksLayout.createSequentialGroup()
                                        .addGroup(iManageBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel26)
                                            .addComponent(jLabel25))
                                        .addGap(18, 18, 18)
                                        .addGroup(iManageBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(txtisbn, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtcategory, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(140, 140, 140)
                                        .addComponent(jLabel34)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtpublish, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(iManageBooksLayout.createSequentialGroup()
                                .addGap(45, 45, 45)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1556, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 32, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, iManageBooksLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(iManageBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, iManageBooksLayout.createSequentialGroup()
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(96, 96, 96)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(107, 107, 107)
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(609, 609, 609))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, iManageBooksLayout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchh, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(93, 93, 93))))
        );
        iManageBooksLayout.setVerticalGroup(
            iManageBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, iManageBooksLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jLabel11)
                .addGap(45, 45, 45)
                .addGroup(iManageBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(iManageBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtbook, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtisbn, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtpublish, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(iManageBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(iManageBooksLayout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addComponent(txttitle, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(iManageBooksLayout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addGroup(iManageBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtcategory, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, iManageBooksLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(34, 34, 34)
                .addGroup(iManageBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtquantity, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtauthor, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 66, Short.MAX_VALUE)
                .addGroup(iManageBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(45, 45, 45)
                .addGroup(iManageBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchh, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 406, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        jTabbedPane1.addTab("tab3", iManageBooks);

        iManageStudents.setBackground(new java.awt.Color(54, 101, 145));
        iManageStudents.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));

        jScrollPane6.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        tstudent.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Id", "Lrn", "Fullname", "Grade lvl", "Section", "Contact Number", "Remarks"
            }
        ));
        tstudent.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tstudentMouseClicked(evt);
            }
        });
        jScrollPane6.setViewportView(tstudent);

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 50)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("MANAGE STUDENTS");

        jLabel46.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel46.setForeground(new java.awt.Color(255, 255, 255));
        jLabel46.setText("LRN:");

        jLabel47.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel47.setForeground(new java.awt.Color(255, 255, 255));
        jLabel47.setText("FULLNAME:");

        jLabel48.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel48.setForeground(new java.awt.Color(255, 255, 255));
        jLabel48.setText("CONTACT NUMBER:");

        jLabel49.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel49.setForeground(new java.awt.Color(255, 255, 255));
        jLabel49.setText("SECTION:");

        txtfname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtfnameActionPerformed(evt);
            }
        });

        txtgrade.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtgradeActionPerformed(evt);
            }
        });

        txtsection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtsectionActionPerformed(evt);
            }
        });

        jButton10.setBackground(new java.awt.Color(54, 101, 145));
        jButton10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton10.setForeground(new java.awt.Color(255, 255, 255));
        jButton10.setText("ADD");
        jButton10.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jButton11.setBackground(new java.awt.Color(54, 101, 145));
        jButton11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton11.setForeground(new java.awt.Color(255, 255, 255));
        jButton11.setText("UPDATE");
        jButton11.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        archivebtn.setBackground(new java.awt.Color(54, 101, 145));
        archivebtn.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        archivebtn.setForeground(new java.awt.Color(255, 255, 255));
        archivebtn.setText("ARCHIVE");
        archivebtn.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));
        archivebtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                archivebtnActionPerformed(evt);
            }
        });

        jLabel31.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel31.setForeground(new java.awt.Color(255, 255, 255));
        jLabel31.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/searchbutton.png"))); // NOI18N
        jLabel31.setText("SEARCH");

        search2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                search2ActionPerformed(evt);
            }
        });
        search2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                search2KeyTyped(evt);
            }
        });

        jLabel51.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel51.setForeground(new java.awt.Color(255, 255, 255));
        jLabel51.setText("GRADE LVL:");

        txtcontact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtcontactActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout iManageStudentsLayout = new javax.swing.GroupLayout(iManageStudents);
        iManageStudents.setLayout(iManageStudentsLayout);
        iManageStudentsLayout.setHorizontalGroup(
            iManageStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(iManageStudentsLayout.createSequentialGroup()
                .addGroup(iManageStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(iManageStudentsLayout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addGroup(iManageStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(iManageStudentsLayout.createSequentialGroup()
                                .addComponent(jLabel31)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(search2, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(iManageStudentsLayout.createSequentialGroup()
                                .addGroup(iManageStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(iManageStudentsLayout.createSequentialGroup()
                                        .addComponent(jLabel47)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtfname, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(iManageStudentsLayout.createSequentialGroup()
                                        .addComponent(jLabel46, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtlrn, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(iManageStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(iManageStudentsLayout.createSequentialGroup()
                                        .addGap(122, 122, 122)
                                        .addComponent(jLabel51))
                                    .addGroup(iManageStudentsLayout.createSequentialGroup()
                                        .addGap(44, 44, 44)
                                        .addGroup(iManageStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel48)
                                            .addComponent(jLabel49))))
                                .addGap(18, 18, 18)
                                .addGroup(iManageStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtcontact, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                                    .addComponent(txtsection)
                                    .addComponent(txtgrade))
                                .addGap(482, 482, 482))
                            .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 1556, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 33, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(iManageStudentsLayout.createSequentialGroup()
                .addGap(457, 457, 457)
                .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(72, 72, 72)
                .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(78, 78, 78)
                .addComponent(archivebtn, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        iManageStudentsLayout.setVerticalGroup(
            iManageStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, iManageStudentsLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jLabel15)
                .addGap(45, 45, 45)
                .addGroup(iManageStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtgrade, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel51, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37)
                .addGroup(iManageStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtsection, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel49, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtlrn, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel46, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(44, 44, 44)
                .addGroup(iManageStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtfname, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel47, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel48, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtcontact, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 68, Short.MAX_VALUE)
                .addGroup(iManageStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(archivebtn, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(49, 49, 49)
                .addGroup(iManageStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel31)
                    .addComponent(search2, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 406, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        jTabbedPane1.addTab("tab3", iManageStudents);

        jPanel8.setBackground(new java.awt.Color(54, 101, 145));
        jPanel8.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));

        ReturnBooks.setBackground(new java.awt.Color(54, 101, 145));
        ReturnBooks.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jLabel57.setFont(new java.awt.Font("Segoe UI", 0, 55)); // NOI18N
        jLabel57.setForeground(new java.awt.Color(255, 255, 255));
        jLabel57.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel57.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/open book.png"))); // NOI18N
        jLabel57.setText("VIEW RECORDS");
        jLabel57.setToolTipText("");

        jSeparator5.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator5.setForeground(new java.awt.Color(255, 255, 255));
        jSeparator5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));

        jPanel14.setBackground(new java.awt.Color(54, 101, 145));
        jPanel14.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));

        DUEDATES.setPlaceholder("DUE DATE");

        ISSUEDATE.setPlaceholder("ISSUE DATE");

        jLabel28.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(255, 255, 255));
        jLabel28.setText("ISSUE DATE:");

        jLabel43.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel43.setForeground(new java.awt.Color(255, 255, 255));
        jLabel43.setText("DUE DATE:");

        SEARCHH.setBackground(new java.awt.Color(54, 101, 145));
        SEARCHH.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SEARCHH.setForeground(new java.awt.Color(255, 255, 255));
        SEARCHH.setText("SEARCH");
        SEARCHH.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 2, true));
        SEARCHH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SEARCHHActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addGap(268, 268, 268)
                .addComponent(jLabel28)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ISSUEDATE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(146, 146, 146)
                .addComponent(jLabel43)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(DUEDATES, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(SEARCHH, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel14Layout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel14Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(ISSUEDATE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(DUEDATES, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addComponent(SEARCHH, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)))
                .addContainerGap(25, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel43, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36))
        );

        trecords.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        trecords.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "ISBN", "TITLE", "LRN", "STUDENT NAME", "ISSUE DATE", "DUE DATE", "STATUS"
            }
        ));
        jScrollPane7.setViewportView(trecords);

        jButton7.setBackground(new java.awt.Color(54, 101, 145));
        jButton7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton7.setForeground(new java.awt.Color(255, 255, 255));
        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/REPORTS LOGO.png"))); // NOI18N
        jButton7.setText("GENERATE REPORTS");
        jButton7.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 2, true));
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ReturnBooksLayout = new javax.swing.GroupLayout(ReturnBooks);
        ReturnBooks.setLayout(ReturnBooksLayout);
        ReturnBooksLayout.setHorizontalGroup(
            ReturnBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ReturnBooksLayout.createSequentialGroup()
                .addGroup(ReturnBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel57, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(ReturnBooksLayout.createSequentialGroup()
                        .addGroup(ReturnBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(ReturnBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(ReturnBooksLayout.createSequentialGroup()
                                    .addGap(579, 579, 579)
                                    .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 470, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(ReturnBooksLayout.createSequentialGroup()
                                    .addGap(30, 30, 30)
                                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 1585, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 29, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ReturnBooksLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel13)
                .addGap(721, 721, 721))
        );
        ReturnBooksLayout.setVerticalGroup(
            ReturnBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ReturnBooksLayout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(jLabel57, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37)
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(76, 76, 76)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
                .addComponent(jButton7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 495, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(72, 72, 72))
        );

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1656, Short.MAX_VALUE)
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(ReturnBooks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1006, Short.MAX_VALUE)
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(ReturnBooks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        jTabbedPane1.addTab("tab5", jPanel8);

        jPanel9.setBackground(new java.awt.Color(54, 101, 145));
        jPanel9.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));

        trecordsss.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        trecordsss.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "ISBN", "TITLE", "LRN", "STUDENT NAME", "ISSUE DATE", "RETURNED DATE", "DUE DATE", "STATUS"
            }
        ));
        jScrollPane9.setViewportView(trecordsss);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(281, 281, 281)
                .addComponent(jLabel14)
                .addContainerGap(1359, Short.MAX_VALUE))
            .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel9Layout.createSequentialGroup()
                    .addGap(27, 27, 27)
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 1585, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(28, Short.MAX_VALUE)))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(141, 141, 141)
                .addComponent(jLabel14)
                .addContainerGap(804, Short.MAX_VALUE))
            .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel9Layout.createSequentialGroup()
                    .addGap(19, 19, 19)
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 907, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(19, Short.MAX_VALUE)))
        );

        jTabbedPane1.addTab("tab6", jPanel9);

        notif.setBackground(new java.awt.Color(54, 101, 145));
        notif.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));

        javax.swing.GroupLayout notifLayout = new javax.swing.GroupLayout(notif);
        notif.setLayout(notifLayout);
        notifLayout.setHorizontalGroup(
            notifLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1640, Short.MAX_VALUE)
        );
        notifLayout.setVerticalGroup(
            notifLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 945, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("tab7", notif);

        jPanel11.setBackground(new java.awt.Color(54, 101, 145));
        jPanel11.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1640, Short.MAX_VALUE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 945, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("tab8", jPanel11);

        jPanel7.setBackground(new java.awt.Color(54, 101, 145));
        jPanel7.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 2, true));

        jScrollPane5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 0));
        jScrollPane5.setForeground(new java.awt.Color(255, 255, 255));

        tarchive.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 0));
        tarchive.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Student Id", "Lrn", "Fullname", "Grade lvl", "Section", "Contact Number", "Remarks", "Date"
            }
        ));
        jScrollPane5.setViewportView(tarchive);

        jLabel41.setFont(new java.awt.Font("Segoe UI", 1, 50)); // NOI18N
        jLabel41.setForeground(new java.awt.Color(255, 255, 255));
        jLabel41.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel41.setText("ARCHIVE STUDENTS");

        jLabel42.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel42.setForeground(new java.awt.Color(255, 255, 255));
        jLabel42.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/searchbutton.png"))); // NOI18N
        jLabel42.setText("SEARCH");

        search3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                search3ActionPerformed(evt);
            }
        });
        search3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                search3KeyTyped(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(54, 101, 145));
        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jButton3.setForeground(new java.awt.Color(255, 255, 255));
        jButton3.setText("RESTORE");
        jButton3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel41, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(33, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 1577, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel42)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(search3, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(32, 32, 32))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(654, 654, 654))))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel41)
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(search3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel42))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 719, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("tab9", jPanel6);

        getContentPane().add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 90, 1650, 990));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseClicked
        // TODO add your handling code here:
        int response = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to logout?",
        "Confirm Logout",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE
    );

    if (response == JOptionPane.YES_OPTION) {
       
        this.dispose();
        new LibLogin().setVisible(true); 
    }
        
    }//GEN-LAST:event_jLabel5MouseClicked

    private void jLabel4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseClicked
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(0);
        refreshdata();
    }//GEN-LAST:event_jLabel4MouseClicked

    private void jLabel6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseClicked
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(2);
        refreshdata();
    }//GEN-LAST:event_jLabel6MouseClicked

    private void jLabel7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MouseClicked
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(4);
        refreshdata();
    }//GEN-LAST:event_jLabel7MouseClicked

    private void jLabel8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel8MouseClicked
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(5);
        refreshdata();
    }//GEN-LAST:event_jLabel8MouseClicked

    private void jManageUserMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jManageUserMouseClicked
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(1);
        refreshdata();
    }//GEN-LAST:event_jManageUserMouseClicked

    private void jLabel17MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel17MouseClicked
        // TODO add your handling code here
        jTabbedPane1.setSelectedIndex(3);
        refreshdata();
    }//GEN-LAST:event_jLabel17MouseClicked

    private void textPassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textPassActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textPassActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        try {
            String Username = textUser.getText().trim();
            String Password = textPass.getText().trim();
            String role = jComboRole.getSelectedItem().toString().trim();

            
            if (Username.isEmpty() || Password.isEmpty() || role.isEmpty()) {
                JOptionPane.showMessageDialog(rootPane, "No input! Please fill in all fields.");
                return;
            }

            con = LibManSys.getConnection();

            
            String checkQuery = "SELECT COUNT(*) FROM accounts WHERE Username = ?";
            PreparedStatement checkStmt = con.prepareStatement(checkQuery);
            checkStmt.setString(1, Username);

            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            if (count > 0) {
                JOptionPane.showMessageDialog(rootPane, "Username, email, or contact already exists! Please use a different one.");
                return; 
            }

            
            pst = con.prepareStatement("INSERT INTO accounts (Username, Password, Role) VALUES(?,?,?)");
            pst.setString(1, Username);
            pst.setString(2, Password);
            pst.setString(3, role);

            int k = pst.executeUpdate();

            if (k == 1) {
                JOptionPane.showMessageDialog(rootPane, "Record added successfully!");
                textUser.setText("");
                textPass.setText("");
                jComboRole.setSelectedIndex(0);
            } else {
                JOptionPane.showMessageDialog(rootPane, "Record failed");
            }

            loadUsersToTable(); 

        } catch (SQLException ex) {
            Logger.getLogger(LibAdmin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        
          int selectedRow = tableUser.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a user to update.");
            return;
        }

        int userId = Integer.parseInt(tableUser.getValueAt(selectedRow, 0).toString());

        String query = "UPDATE accounts SET Username=?, Password=?, Role=? WHERE ID=?";

        try {
            con = LibManSys.getConnection();
            PreparedStatement pst = con.prepareStatement(query);

            pst.setString(1, textUser.getText());
            pst.setString(2, textPass.getText());
            pst.setString(3, jComboRole.getSelectedItem().toString());
            pst.setInt(4, userId);

            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(null, "User updated successfully.");
                loadUsersToTable();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error updating user.");
        }   
    }//GEN-LAST:event_jButton2ActionPerformed

    private void tableUserMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableUserMouseClicked
        // TODO add your handling code here:
            int selectedRow = tableUser.getSelectedRow();
    
    if (selectedRow >= 0) {
        int Id = Integer.parseInt(tableUser.getValueAt(selectedRow, 0).toString()); 
        fetchUserData(Id); 
    }
    }//GEN-LAST:event_tableUserMouseClicked

    private void jComboRoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboRoleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboRoleActionPerformed

    private void txtauthorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtauthorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtauthorActionPerformed

    private void txtquantityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtquantityActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtquantityActionPerformed

    private void txtcategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtcategoryActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtcategoryActionPerformed

    private void txtisbnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtisbnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtisbnActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        try {
            String bookid = txtbook.getText().trim();
            String title = txttitle.getText().trim();
            String author = txtauthor.getText().trim();
            String isbn = txtisbn.getText().trim();
            String category = txtcategory.getText().trim();
            String copies = txtquantity.getText().trim();
            Date date = txtpublish.getDate();
            String publish = (date != null) ? new SimpleDateFormat("yyyy-MM-dd").format(date) : "";

            if (bookid.isEmpty() || title.isEmpty() || author.isEmpty() || isbn.isEmpty() ||
                category.isEmpty() || copies.isEmpty() || publish.isEmpty()) {
                JOptionPane.showMessageDialog(rootPane, "No input! Please fill in all fields.");
                return;
            }

            int totalCopies = Integer.parseInt(copies);

            con = LibManSys.getConnection();

            String insertQuery = "INSERT INTO books (book_id, title, author, isbn, category, quantity, published_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
            pst = con.prepareStatement(insertQuery);
            pst.setString(1, bookid);
            pst.setString(2, title);
            pst.setString(3, author);
            pst.setString(4, isbn);
            pst.setString(5, category);
            pst.setInt(6, totalCopies);
            pst.setString(7, publish);

            int k = pst.executeUpdate();

            if (k == 1) {
                JOptionPane.showMessageDialog(rootPane, "Book added successfully!");

               
                txtbook.setText("");
                txttitle.setText("");
                txtauthor.setText("");
                txtisbn.setText("");
                txtcategory.setText("");
                txtquantity.setText("");
                txtpublish.setDate(null); 
            } else {
                JOptionPane.showMessageDialog(rootPane, "Failed to add book!");
            }

            loadBooks();

        } catch (SQLException ex) {
            Logger.getLogger(LibAdmin.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(rootPane, "Invalid number format for copies!");
        }

    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        int selectedRow = tbooks.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a book to update.");
            return;
        }

        int bookid = Integer.parseInt(tbooks.getValueAt(selectedRow, 0).toString());


        String title = txttitle.getText().trim();
        String author = txtauthor.getText().trim();
        String isbn = txtisbn.getText().trim();
        String category = txtcategory.getText().trim();
        String copiesText = txtquantity.getText().trim();
        Date date = txtpublish.getDate();
            String publish = (date != null) ? new SimpleDateFormat("yyyy-MM-dd").format(date) : "";

        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || category.isEmpty() || copiesText.isEmpty() || publish.isEmpty()) {
            JOptionPane.showMessageDialog(null, "All fields must be filled out!");
            return;
        }

        try {
            int totalCopies = Integer.parseInt(copiesText);
            

            String query = "UPDATE books SET title=?, author=?, isbn=?, category=?, quantity=?, published_date=? WHERE book_id=?";

            con = LibManSys.getConnection();
            PreparedStatement pst = con.prepareStatement(query);

            pst.setString(1, title);
            pst.setString(2, author);
            pst.setString(3, isbn);
            pst.setString(4, category);
            pst.setInt(5, totalCopies);
            pst.setString(6, publish);
            pst.setInt(7, bookid);

            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(null, "Book updated successfully.");
                        txttitle.setText("");
                        txtauthor.setText("");
                        txtisbn.setText("");
                        txtcategory.setText("");
                        txtquantity.setText("");
                        txtpublish.setDate(null);
                        
                loadBooks(); 
            } else {
                JOptionPane.showMessageDialog(null, "No book was updated. Please check the ID.");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid number format for copies! Please enter valid numbers.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error updating book.");
        } 

    
    }//GEN-LAST:event_jButton5ActionPerformed

    private void tbooksMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbooksMouseClicked
        // TODO add your handling code here:
        int selectedRow = tbooks.getSelectedRow();

        if (selectedRow >= 0) {
            int bookId = Integer.parseInt(tbooks.getValueAt(selectedRow, 0).toString()); 
            fetchBookData(bookId); 
        }
    }//GEN-LAST:event_tbooksMouseClicked

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        int selectedRow = tbooks.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a user to delete.");
            return;
        }

        int userId = Integer.parseInt(tbooks.getValueAt(selectedRow, 0).toString());

        int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this user?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String query = "DELETE FROM books WHERE book_id=?";

            try {
                con = LibManSys.getConnection();
                PreparedStatement pst = con.prepareStatement(query);
                pst.setInt(1, userId);

                int rowsDeleted = pst.executeUpdate();
                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(null, "User deleted successfully."); 
                }
                loadBooks();

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error deleting user.");
            }
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jLabel29MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel29MouseClicked
        // TODO add your handling code here:
  
    }//GEN-LAST:event_jLabel29MouseClicked

    private void txtbookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtbookActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtbookActionPerformed

    private void tstudentMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tstudentMouseClicked
        // TODO add your handling code here:       
    
    int selectedRow = tstudent.getSelectedRow();

    if (selectedRow >= 0) {
        
        String lrnString = tstudent.getValueAt(selectedRow, 0).toString().trim();

        
        try {
            long lrn = Long.parseLong(lrnString);  
            fetchStudentData(lrn);  
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid LRN format.");
        }
    }

    }//GEN-LAST:event_tstudentMouseClicked

    private void txtfnameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtfnameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtfnameActionPerformed

    private void txtgradeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtgradeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtgradeActionPerformed

    private void txtsectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtsectionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtsectionActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        // TODO add your handling code here:
        try {
            String lrn = txtlrn.getText().trim();
            String fullname = txtfname.getText().trim();
            String gradelvl = txtgrade.getText().trim();
            String section = txtsection.getText().trim();
            String contact = txtcontact.getText().trim();

            
            if (lrn.isEmpty() || fullname.isEmpty() || gradelvl.isEmpty() || section.isEmpty() || contact.isEmpty()) {
                JOptionPane.showMessageDialog(rootPane, "No input! Please fill in all fields.");
                return;
            }

            
            

            
            con = LibManSys.getConnection();

            
            String insertQuery = "INSERT INTO student ( lrn, fullname, grade_lvl, section, contact_number) VALUES (?, ?, ?, ?, ?)";
            pst = con.prepareStatement(insertQuery);
            pst.setString(1, lrn);
            pst.setString(2, fullname);
            pst.setString(3, gradelvl);
            pst.setString(4, section);
            pst.setString(5, contact);

            int k = pst.executeUpdate();

            if (k == 1) {
                JOptionPane.showMessageDialog(rootPane, "Student added successfully!");
                
                txtlrn.setText("");
                txtfname.setText("");
                txtgrade.setText("");
                txtsection.setText("");
                txtcontact.setText("");
            } else {
                JOptionPane.showMessageDialog(rootPane, "Failed to student book!");
            }
              loadStudentss();
        } catch (SQLException ex) {
            Logger.getLogger(LibAdmin.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(rootPane, "Invalid number format for copies!");
        }
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        // TODO add your handling code here:                                       
            int selectedRow = tstudent.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Please select a Student to update.");
                return;
            }

            // Get student details
            int studentid = Integer.parseInt(tstudent.getValueAt(selectedRow, 0).toString()); // Assuming the student_id is in the first column
            String lrn = txtlrn.getText().trim();
            String fullname = txtfname.getText().trim();
            String gradelvl = txtgrade.getText().trim();
            String section = txtsection.getText().trim();
            String contact = txtcontact.getText().trim();

            // Validate if any field is empty
            if (lrn.isEmpty() || fullname.isEmpty() || gradelvl.isEmpty() || section.isEmpty() || contact.isEmpty()) {
                JOptionPane.showMessageDialog(null, "All fields must be filled out!");
                return;
            }

            try {
                // Prepare SQL query to update student data
                String query = "UPDATE student SET lrn=?, fullname=?, grade_lvl=?, section=?, contact_number=? WHERE student_id=?";

                con = LibManSys.getConnection();
                PreparedStatement pst = con.prepareStatement(query);

                
                pst.setString(1, lrn);  
                pst.setString(2, fullname);
                pst.setString(3, gradelvl);
                pst.setString(4, section);
                pst.setString(5, contact);
                pst.setInt(6, studentid); 

                // Execute the update query
                int rowsUpdated = pst.executeUpdate();
                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(null, "Student updated successfully.");
                    
                    txtlrn.setText("");
                    txtfname.setText("");
                    txtgrade.setText("");
                    txtsection.setText("");
                    txtcontact.setText("");

                    loadStudentss();  
                } else {
                    JOptionPane.showMessageDialog(null, "No Student was updated. Please check the ID.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error updating Student.");
            }


    }//GEN-LAST:event_jButton11ActionPerformed

    private void archivebtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_archivebtnActionPerformed
        // TODO add your handling code here:
        int selectedRow = tstudent.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a student to inactive.");
            return;
        }

        
        int students_id = Integer.parseInt(tstudent.getValueAt(selectedRow, 0).toString());
        String lrn = tstudent.getValueAt(selectedRow, 1).toString();
        String fullname = tstudent.getValueAt(selectedRow, 2).toString(); 
        String gradelvl = tstudent.getValueAt(selectedRow, 3).toString();
        String section = tstudent.getValueAt(selectedRow, 4).toString();
        String contact = tstudent.getValueAt(selectedRow, 5).toString();
        String remarks = tstudent.getValueAt(selectedRow, 6).toString();

        
        int confirm = JOptionPane.showConfirmDialog(null, 
            "Are you sure you want to inactive this student?", 
            "Confirm Inactive", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Connection con = null;
            PreparedStatement pstInsert = null;
            PreparedStatement pstDelete = null;

            try {
                con = LibManSys.getConnection();
                con.setAutoCommit(false); 

                
                String insertQuery = "INSERT INTO inactive_students (student_id, lrn, fullname, grade_lvl, section,contact_number) " +
                                     "VALUES (?, ?, ?, ?, ?, ?)";
                pstInsert = con.prepareStatement(insertQuery);
                pstInsert.setInt(1, students_id);
                pstInsert.setString(2, lrn);
                pstInsert.setString(3, fullname);
                pstInsert.setString(4, gradelvl);
                pstInsert.setString(5, section);
                pstInsert.setString(6, contact);
                pstInsert.executeUpdate();

                
                String deleteQuery = "DELETE FROM student WHERE student_id=?";
                pstDelete = con.prepareStatement(deleteQuery);
                pstDelete.setInt(1, students_id);
                pstDelete.executeUpdate();

                con.commit(); 

                JOptionPane.showMessageDialog(null, "Student dropped out successfully.");
                txtlrn.setText("");
                txtfname.setText("");
                txtgrade.setText("");
                txtsection.setText("");
                txtcontact.setText("");
                loadStudentss(); 
                loadInactiveStudents();
            } catch (SQLException e) {
                if (con != null) {
                    try {
                        con.rollback(); 
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error inactive student.");
            } finally {
                try {
                    if (pstInsert != null) pstInsert.close();
                    if (pstDelete != null) pstDelete.close();
                    if (con != null) con.setAutoCommit(true); 
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }


    }//GEN-LAST:event_archivebtnActionPerformed

    private void searchhActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchhActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_searchhActionPerformed

    private void searchhKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchhKeyTyped
        // TODO add your handling code here:
                    DefaultTableModel model = (DefaultTableModel) tbooks.getModel();
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            tbooks.setRowSorter(sorter);

           
            String query = searchh.getText().trim();

            if (!query.isEmpty()) {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(query)));
            } else {
                sorter.setRowFilter(null); // Show all rows when search is empty
            }

    }//GEN-LAST:event_searchhKeyTyped

    private void search2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_search2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_search2ActionPerformed

    private void search2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_search2KeyTyped
        // TODO add your handling code here:
        DefaultTableModel model = (DefaultTableModel) tstudent.getModel();
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            tstudent.setRowSorter(sorter);

           
            String query = search2.getText().trim();

            if (!query.isEmpty()) {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(query)));
            } else {
                sorter.setRowFilter(null); 
            }
    }//GEN-LAST:event_search2KeyTyped

    private void jLabel35MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel35MouseClicked
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(8);
    }//GEN-LAST:event_jLabel35MouseClicked

    private void search3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_search3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_search3ActionPerformed

    private void search3KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_search3KeyTyped
        // TODO add your handling code here:
        DefaultTableModel model = (DefaultTableModel) tstudent.getModel();
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            tarchive.setRowSorter(sorter);

           
            String query = search3.getText().trim();

            if (!query.isEmpty()) {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(query)));
            } else {
                sorter.setRowFilter(null); 
            }
    }//GEN-LAST:event_search3KeyTyped

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        int selectedRow = tarchive.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a student to restore.");
            return;
        }

       
        int students_id = Integer.parseInt(tarchive.getValueAt(selectedRow, 0).toString());
        String lrn = tarchive.getValueAt(selectedRow, 1).toString();
        String fullname = tarchive.getValueAt(selectedRow, 2).toString(); 
        String gradelvl = tarchive.getValueAt(selectedRow, 3).toString();
        String section = tarchive.getValueAt(selectedRow, 4).toString();
        String contact = tarchive.getValueAt(selectedRow, 5).toString();       

        
        int confirm = JOptionPane.showConfirmDialog(null, 
            "Are you sure you want to restore this student?", 
            "Confirm Restore", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Connection con = null;
            PreparedStatement pstInsert = null;
            PreparedStatement pstDelete = null;

            try {
                con = LibManSys.getConnection();
                con.setAutoCommit(false); 

                
                String insertQuery = "INSERT INTO student (student_id, lrn, fullname, grade_lvl, section, contact_number) " +
                                     "VALUES (?, ?, ?, ?, ?, ?)";
                pstInsert = con.prepareStatement(insertQuery);
                pstInsert.setInt(1, students_id);
                pstInsert.setString(2, lrn);
                pstInsert.setString(3, fullname); 
                pstInsert.setString(4, gradelvl);
                pstInsert.setString(5, section);
                pstInsert.setString(6, contact);
                pstInsert.executeUpdate();

                
                String deleteQuery = "DELETE FROM inactive_students WHERE student_id=?";
                pstDelete = con.prepareStatement(deleteQuery);
                pstDelete.setInt(1, students_id);
                pstDelete.executeUpdate();

                con.commit(); 

                JOptionPane.showMessageDialog(null, "Student restored successfully.");
                loadInactiveStudents(); 
                loadStudentss();
            } catch (SQLException e) {
                if (con != null) {
                    try {
                        con.rollback(); 
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error restoring student.");
            } finally {
                try {
                    if (pstInsert != null) pstInsert.close();
                    if (pstDelete != null) pstDelete.close();
                    if (con != null) con.setAutoCommit(true); 
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void txtcontactActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtcontactActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtcontactActionPerformed

    private void SEARCHHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SEARCHHActionPerformed
        // TODO add your handling code here:
        searchh();
    }//GEN-LAST:event_SEARCHHActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
        exportToPDF(trecords);
    }//GEN-LAST:event_jButton7ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LibAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LibAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LibAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LibAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LibAdmin().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private rojeru_san.componentes.RSDateChooser DUEDATES;
    private rojeru_san.componentes.RSDateChooser ISSUEDATE;
    private javax.swing.JPanel ReturnBooks;
    private javax.swing.JButton SEARCHH;
    private rojerusan.RSTableMetro STUDENT;
    private javax.swing.JButton archivebtn;
    private javax.swing.JLabel date;
    private javax.swing.JPanel iDashboard;
    private javax.swing.JPanel iManageBooks;
    private javax.swing.JPanel iManageStudents;
    private javax.swing.JPanel iManageUSer;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JComboBox<String> jComboRole;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jManageUser;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel nobooks;
    private javax.swing.JLabel nostudents;
    private javax.swing.JPanel notif;
    private javax.swing.JPanel pPie;
    private javax.swing.JTextField search2;
    private javax.swing.JTextField search3;
    private javax.swing.JTextField searchh;
    private javax.swing.JTable tableUser;
    private javax.swing.JTable tarchive;
    private javax.swing.JTable tbooks;
    private javax.swing.JTextField textPass;
    private javax.swing.JTextField textUser;
    private javax.swing.JLabel time;
    private javax.swing.JTable trecords;
    private javax.swing.JTable trecordsss;
    private javax.swing.JTable tstudent;
    private javax.swing.JTextField txtauthor;
    private javax.swing.JTextField txtbook;
    private javax.swing.JTextField txtcategory;
    private javax.swing.JTextField txtcontact;
    private javax.swing.JTextField txtfname;
    private javax.swing.JTextField txtgrade;
    private javax.swing.JTextField txtisbn;
    private javax.swing.JTextField txtlrn;
    private com.toedter.calendar.JDateChooser txtpublish;
    private javax.swing.JTextField txtquantity;
    private javax.swing.JTextField txtsection;
    private javax.swing.JTextField txttitle;
    // End of variables declaration//GEN-END:variables
}
