import javax.swing.*;
import java.sql.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import java.io.FileOutputStream;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import java.text.SimpleDateFormat;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author Eriann
 */


public class LibUser extends javax.swing.JFrame {

    /**
     * Creates new form LibUser
     */
    // ADD STUDENT
    Connection con;
    PreparedStatement pst;
    ResultSet rs;
    public LibUser() {
        initComponents();
        loadStudentss();
        showPieChart();
        countTotalBooks();
        deleteBook();
        loadBookss();
        loadtimer();
        countTotalStudent();
        loadStudentsss();
        deleteStudent();
        loadtstudenttime();
        getbooks(); 
        getstudent();
        loadBooks();
        Viewrecords();
        showOverdueBooks();
        studentdetails();
        showDate();
        showTime();
    }
    
    public void showTermsAndConditions() {
    String terms = "📚 Library Book Borrowing System - Terms and Conditions\n\n"
        + "1. Eligibility:\n"
        + "   - Only enrolled students and authorized staff may borrow books.\n"
        + "   - A valid Student ID (LRN) is required.\n\n"
        + "2. Borrowing Limits:\n"
        + "   - A student may borrow a maximum of 1 books at a time.\n"
        + "   - Each book may be borrowed for a maximum of 7 days only.\n\n"
        + "3. Renewals:\n"
        + "   - Books may be renewed once if no other reservation exists.\n"
        + "   - Renewal must be requested before the due date.\n\n"
        + "4. Returns:\n"
        + "   - Books must be returned on or before the due date.\n"
        + "   - Late returns may result in penalties.\n\n"
        + "5. Overdue and Penalties:\n"
        + "   - ₱2.00 fine per day for each overdue book.\n"
        + "   - After 30 days overdue, the book will be considered lost.\n\n"
        + "6. Lost or Damaged Books:\n"
        + "   - Borrower must pay for lost or damaged books based on the book’s current value.\n\n"
        + "7. Book Availability:\n"
        + "   - Reference books and special materials are not available for borrowing.\n\n"
        + "8. Misuse of the System:\n"
        + "   - Providing false information or attempting to manipulate the system will result in disciplinary action.\n\n"
        + "9. System Use:\n"
        + "   - All borrow and return activities are monitored by the system.\n\n"
        + "10. Policy Updates:\n"
        + "   - The library reserves the right to change policies without prior notice.\n";

    JTextArea textArea = new JTextArea(terms);
    textArea.setEditable(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);

    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setPreferredSize(new Dimension(500, 400));

    JOptionPane.showMessageDialog(null, scrollPane, "Terms and Conditions", JOptionPane.INFORMATION_MESSAGE);
}

    


    
    public void refreshdata(){
        loadStudentss();
        loadBookss();
        loadStudentsss();
        loadBooks();
        Viewrecords();
        showOverdueBooks();
        studentdetails();
    }
    //notif
    


    
     //time
     public void showTime(){
        new Timer(0, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                Date d = new Date();
                SimpleDateFormat s = new SimpleDateFormat("hh-mm-ss");
                String tim = s.format(d);
                time.setText(tim);
            }
        }).start();
    }

     
     //date
     public void showDate(){
        Date d = new Date();
        SimpleDateFormat s = new SimpleDateFormat("dd-MM-yyyy");
        String dat = s.format(d);
        date.setText(dat);
}
    
    //DASHBOARD TABLE
    public void studentdetails() {
    DefaultTableModel model = (DefaultTableModel) studentdetails.getModel();
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
            model = (DefaultTableModel) studentdetails.getModel();
            model.addRow(row);
            
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
    
    // OVERDUE BOOKS
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
            String returndate = rs.getString("returned_date");
            String duedate = rs.getString("due_date");
            String status = rs.getString("status");

            Object[] row = {id, isbn, title, lrn, studentname, issuedate, returndate, duedate, status};
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
            model = (DefaultTableModel) trecords.getModel();
            model.addRow(row);
            }

        } catch (Exception e) {
            e.printStackTrace(); 
        }

            }
    
    
    //BOOKS
           

        public void loadBooks() {
            DefaultTableModel model = (DefaultTableModel) tbooks.getModel();
            model.setRowCount(0);  // Clear previous rows

            String query = "SELECT book_id, title, author, isbn, category, quantity, published_date FROM books ORDER BY book_id DESC"; 

            try (
                Connection con = LibManSys.getConnection();  
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

                // Date format to display it in a user-friendly format
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getString("category"),
                        rs.getInt("quantity"),
                        // Formatting the published date to a readable format
                        dateFormat.format(rs.getDate("published_date"))
                    };
                    model.addRow(row);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


          //RETURN BOOK
          public boolean returnBook(){
              boolean isReturned = false;
              int bookid = Integer.parseInt(booksid.getText());
              int studentid = Integer.parseInt(studentsid.getText());

              try {
                  Connection con = LibManSys.getConnection();
                  String sql = "update issue_book_details set status = ? where student_id = ? and book_id = ? and status = ? ";
                  PreparedStatement pst = con.prepareStatement(sql);
                  pst.setString(1, "Returned");
                  pst.setInt(2, studentid);
                  pst.setInt(3, bookid);
                  pst.setString(4, "Pending");

                  int rowCount = pst.executeUpdate();
                  if (rowCount > 0) {
                      isReturned = true;

                  }else{
                  isReturned = false;
                  }
              } catch (Exception e) {
                  e.printStackTrace();
              }
              return isReturned;
          }

          //FETCH ISSUE BOOK
          
         public void getIssuebookdetails() {
          String bookIdText = booksid.getText();
          String studentIdText = studentsid.getText();

          if (bookIdText.isEmpty() || studentIdText.isEmpty()) {
              bookerror1.setText("Please enter both Book ID and Student ID.");
              return;
          }

          try {
              long isbn = Long.parseLong(booksid.getText().trim());
            long lrn = Long.parseLong(studentsid.getText().trim());

              Connection con = LibManSys.getConnection();
              String sql = "SELECT * FROM issue_book_details WHERE isbn = ? AND lrn = ? AND status = ?";
              PreparedStatement pst = con.prepareStatement(sql);
              pst.setLong(1, isbn);
              pst.setLong(2, lrn);
              pst.setString(3, "Borrowed");

              ResultSet rs = pst.executeQuery();
              if (rs.next()) {
                  lblissueid.setText(rs.getString("student_name"));
                  lbltitles.setText(rs.getString("title"));
                  lblstudent.setText(rs.getString("lrn"));
                  lblissuedate.setText(rs.getString("issue_date"));
                  lblduedate.setText(rs.getString("due_date"));
              } else {
                  bookerror1.setText("No Record Found");
                  lblissueid.setText("");
                  lbltitles.setText("");
                  lblstudent.setText("");
                  lblissuedate.setText("");
                  lblduedate.setText("");
              }

              rs.close();
              pst.close();
              con.close();

          } catch (NumberFormatException e) {
              bookerror1.setText("Invalid Id.");
          } catch (Exception e) {
              e.printStackTrace();
              bookerror1.setText("Something went wrong.");
          }
      }


    
    
    //BORROW BOOKS
       public void getbooks() {
    try {
        String bookIdText = txtbookid.getText().trim();
        if (!bookIdText.isEmpty()) {
            long bookid = Long.parseLong(bookIdText);

            Connection con = LibManSys.getConnection();
            PreparedStatement pst = con.prepareStatement("SELECT * FROM books WHERE isbn = ?");
            pst.setLong(1, bookid); 
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                lbltitle.setText(rs.getString("title"));
                lblauthor.setText(rs.getString("author"));
                lblisbn.setText(rs.getString("isbn"));
                lblcategory.setText(rs.getString("category"));
                lblquantity.setText(String.valueOf(rs.getInt("quantity")));

                java.sql.Date sqlDate = rs.getDate("published_date");
                lblpublish.setText(sqlDate != null ? sqlDate.toString() : "");

                bookerror.setText("");
            } else {
                bookerror.setText("Invalid Book ID!");
            }
        }
    } catch (NumberFormatException e) {
        bookerror.setText("Book ID must be a valid number.");
    } catch (Exception e) {
        e.printStackTrace();
        bookerror.setText("An error occurred while fetching book info.");
    }
}


        
        //INSERT BOOKS
public boolean issuebook() {
    boolean isIssued = false;

    try {
       
        long isbn = Long.parseLong(lblisbn.getText().trim());
        long lrn = Long.parseLong(lbllrn.getText().trim());
        String title = lbltitle.getText().trim();
        String fname = lblfname.getText().trim();

    
        Date uIssuedDate = issuedate.getDatoFecha();
        Date uDueDate = duedate.getDatoFecha();

       
        if (uIssuedDate == null || uDueDate == null) {
            JOptionPane.showMessageDialog(null, "Please select both issue and due dates.");
            return false;
        }

        if (uDueDate.before(uIssuedDate)) {
            JOptionPane.showMessageDialog(null, "Due date cannot be before issue date.");
            return false;
        }

      
        java.sql.Date sIssueDate = new java.sql.Date(uIssuedDate.getTime());
        java.sql.Date sDueDate = new java.sql.Date(uDueDate.getTime());

        
        Connection con = LibManSys.getConnection();
        PreparedStatement checkQtyPst = con.prepareStatement("SELECT quantity FROM books WHERE isbn = ?");
        checkQtyPst.setLong(1, isbn);
        ResultSet rs = checkQtyPst.executeQuery();

        if (rs.next()) {
            int quantity = rs.getInt("quantity");
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(null, "This book is currently unavailable.");
                rs.close();
                checkQtyPst.close();
                con.close();
                return false;
            }
        } else {
            JOptionPane.showMessageDialog(null, "Book not found.");
            rs.close();
            checkQtyPst.close();
            con.close();
            return false;
        }

        rs.close();
        checkQtyPst.close();

        // ✅ Step 2: Proceed with issuing the book
        String sql = "INSERT INTO issue_book_details (isbn, title, lrn, student_name, issue_date, returned_date, due_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?,?)";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setLong(1, isbn);
        pst.setString(2, title);
        pst.setLong(3, lrn);
        pst.setString(4, fname);
        pst.setDate(5, sIssueDate);
        pst.setDate(6, null);
        pst.setDate(7, sDueDate);
        pst.setString(8, "Borrowed");

        int rowCount = pst.executeUpdate();
        isIssued = (rowCount > 0);

        // 🔄 Optional: Update book quantity if issue was successful
        if (isIssued) {
            updatebookcount();
        }

        pst.close();
        con.close();

    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "Please enter valid numeric values for LRN and ISBN.");
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "An error occurred while issuing the book.");
    }

    return isIssued;
}


public void updatebookcount() {
    try {
        long isbn = Long.parseLong(lblisbn.getText().trim());

        Connection con = LibManSys.getConnection();
        String sql = "UPDATE books SET quantity = quantity - 1 WHERE isbn = ?";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setLong(1, isbn);

        int rowCount = pst.executeUpdate();

        if (rowCount > 0) {
            JOptionPane.showMessageDialog(this, "Book Count Updated");
            int initialCount = Integer.parseInt(lblquantity.getText());
            lblquantity.setText(Integer.toString(initialCount - 1));
        } else {
            JOptionPane.showMessageDialog(this, "Can't Update Book Count");
        }

        pst.close();
        con.close();

    } catch (Exception e) {
        e.printStackTrace();
    }
}


        public boolean returnBookandupdatequantity() {
    boolean isReturned = false;

    try {
        long bookId = Long.parseLong(booksid.getText().trim());
        long lrn = Long.parseLong(studentsid.getText().trim());

        Connection con = LibManSys.getConnection();

        // Get only the latest 'Borrowed' record
        String checkSql = "SELECT * FROM issue_book_details WHERE isbn = ? AND lrn = ? AND status = 'Borrowed' ORDER BY issue_date DESC LIMIT 1";
        PreparedStatement checkStmt = con.prepareStatement(checkSql);
        checkStmt.setLong(1, bookId);
        checkStmt.setLong(2, lrn);
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            int issueId = rs.getInt("id"); // <-- Use unique ID to update exact row

            // Update only the matching issue_id
            String updateStatusSql = "UPDATE issue_book_details SET status = 'Returned', returned_date = CURRENT_TIMESTAMP WHERE id = ?";
            PreparedStatement updateStatus = con.prepareStatement(updateStatusSql);
            updateStatus.setInt(1, issueId);
            updateStatus.executeUpdate();

            // Update book quantity
            String updateBookSql = "UPDATE books SET quantity = quantity + 1 WHERE isbn = ?";
            PreparedStatement updateBook = con.prepareStatement(updateBookSql);
            updateBook.setLong(1, bookId);
            int rowCount = updateBook.executeUpdate();

            if (rowCount > 0) {
                JOptionPane.showMessageDialog(null, "Book Returned Successfully");
                isReturned = true;
            }

            updateStatus.close();
            updateBook.close();
        } else {
            JOptionPane.showMessageDialog(null, "No matching record found or book already returned.");
        }

        // Clear all form fields
        lblissueid.setText("");
        lbltitles.setText("");
        lblstudent.setText("");
        lblissuedate.setText("");
        lblduedate.setText("");
        booksid.setText("");
        studentsid.setText("");

        rs.close();
        checkStmt.close();
        con.close();

    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "Please enter valid numeric values.");
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "An error occurred while processing the return.");
    }

    return isReturned;
}
        //UPDATE BOOK COUNT
       
        


        
        // CHECKING IF BOOK ALREADY BORROWED
        
        public boolean alreadytaken() {
            boolean alreadyTaken = false;
            String bookIdText = txtbookid.getText().trim();
            String studentIdText = txtstudentid.getText().trim();

            if (bookIdText.isEmpty() || studentIdText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both Book ID and Student ID.");
                return alreadyTaken;
            }

            int bookid = Integer.parseInt(bookIdText);
            int studentid = Integer.parseInt(studentIdText);

            try (Connection con = LibManSys.getConnection();
                 PreparedStatement pst = con.prepareStatement("SELECT * FROM issue_book_details WHERE book_id = ? AND student_id = ? AND status = ?")) {

                pst.setInt(1, bookid);
                pst.setInt(2, studentid);
                pst.setString(3, "Pending");

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        alreadyTaken = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return alreadyTaken;
        }

        public void getstudent() {
            String studentIdText = txtstudentid.getText().trim();
            if (studentIdText.isEmpty()) {
                studenterror.setText("Please enter a valid Student ID.");
                return;
            }

            try (Connection con = LibManSys.getConnection();
                 PreparedStatement pst = con.prepareStatement("SELECT * FROM student WHERE lrn = ?")) {

                long studentid = Long.parseLong(studentIdText);
                pst.setLong(1, studentid);

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        lbllrn.setText(rs.getString("lrn"));
                        lblfname.setText(rs.getString("fullname"));
                        lblgrade.setText(rs.getString("grade_lvl"));
                        lblsec.setText(rs.getString("section"));
                        lblcontact.setText(rs.getString("contact_number"));

                        studenterror.setText("");
                    } else {
                        studenterror.setText("Invalid Student Id!");
                    }
                }
            } catch (NumberFormatException e) {
                studenterror.setText("LRN must be a number.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        
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
            Timer timer = new Timer(5000, new ActionListener() {
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

            Timer timer = new Timer(5000, new ActionListener() {
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
    
     public void loadStudentss() {
        DefaultTableModel model = (DefaultTableModel) tstudent.getModel();
        model.setRowCount(0); 

        String query = "SELECT lrn, fullname, grade_lvl, section, contact_number, remarks FROM student ORDER BY student_id DESC"; 

        try (
            Connection con = LibManSys.getConnection();  
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Object[] row = {
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
     
   private void fetchStudentData(int studentId) {
    String query = "SELECT lrn, fullname, grade_lvl, section, remarks  FROM student WHERE student_id = ?";

    try (Connection con = LibManSys.getConnection();
         PreparedStatement pst = con.prepareStatement(query)) {

        pst.setInt(1, studentId);  

        try (ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                txtlrn.setText(rs.getString("lrn"));
                txtfname.setText(rs.getString("fullname"));
                txtgrade.setText(rs.getString("grade_lvl"));
                txtsection.setText(rs.getString("section"));
                
            } else {
                JOptionPane.showMessageDialog(null, "No Student found with this ID.");
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error fetching student data: " + e.getMessage());
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

        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        termscondition = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        Dashboard = new javax.swing.JPanel();
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
        studentdetails = new rojerusan.RSTableMetro();
        pPie = new javax.swing.JPanel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        addStudents = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        txtsection = new javax.swing.JTextField();
        jButton10 = new javax.swing.JButton();
        jLabel46 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        txtlrn = new javax.swing.JTextField();
        jScrollPane6 = new javax.swing.JScrollPane();
        tstudent = new javax.swing.JTable();
        txtfname = new javax.swing.JTextField();
        txtgrade = new javax.swing.JTextField();
        jLabel50 = new javax.swing.JLabel();
        txtsection1 = new javax.swing.JTextField();
        Books = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbooks = new javax.swing.JTable();
        searchh = new javax.swing.JTextField();
        jLabel55 = new javax.swing.JLabel();
        jLabel56 = new javax.swing.JLabel();
        BookBorrow = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel11 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        lbltitle = new javax.swing.JLabel();
        lblauthor = new javax.swing.JLabel();
        lblisbn = new javax.swing.JLabel();
        lblquantity = new javax.swing.JLabel();
        lblpublish = new javax.swing.JLabel();
        lblcategory = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        bookerror = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel35 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        lblfname = new javax.swing.JLabel();
        lblgrade = new javax.swing.JLabel();
        lblsec = new javax.swing.JLabel();
        lblcontact = new javax.swing.JLabel();
        lbllrn = new javax.swing.JLabel();
        studenterror = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        txtbookid = new javax.swing.JTextField();
        txtstudentid = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        duedate = new rojeru_san.componentes.RSDateChooser();
        issuedate = new rojeru_san.componentes.RSDateChooser();
        bookborrows = new javax.swing.JButton();
        ReturnBooks = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jSeparator5 = new javax.swing.JSeparator();
        jLabel31 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        lbltitles = new javax.swing.JLabel();
        lblstudent = new javax.swing.JLabel();
        lblissuedate = new javax.swing.JLabel();
        lblissueid = new javax.swing.JLabel();
        lblduedate = new javax.swing.JLabel();
        jLabel57 = new javax.swing.JLabel();
        bookerror1 = new javax.swing.JLabel();
        returnbook = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JSeparator();
        jLabel58 = new javax.swing.JLabel();
        booksid = new javax.swing.JTextField();
        studentsid = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel59 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        finddetails = new javax.swing.JButton();
        Record = new javax.swing.JPanel();
        jSeparator7 = new javax.swing.JSeparator();
        jLabel62 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        trecords = new javax.swing.JTable();
        jPanel14 = new javax.swing.JPanel();
        DUEDATES = new rojeru_san.componentes.RSDateChooser();
        ISSUEDATE = new rojeru_san.componentes.RSDateChooser();
        jLabel28 = new javax.swing.JLabel();
        jLabel61 = new javax.swing.JLabel();
        SEARCHH = new javax.swing.JButton();
        BookIssues = new javax.swing.JPanel();
        jScrollPane9 = new javax.swing.JScrollPane();
        trecordsss = new javax.swing.JTable();
        jPanel8 = new javax.swing.JPanel();
        jLabel63 = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        trecords1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(54, 101, 145));
        jPanel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 2, true));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 50)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/header logo.png"))); // NOI18N
        jLabel3.setText("  SAUYO HIGH SCHOOL");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 1916, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1920, 114));

        jPanel3.setBackground(new java.awt.Color(54, 101, 145));
        jPanel3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/ADMIN LOGO.png"))); // NOI18N
        jLabel2.setText("STAFF");
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/DASHBOARD LOGO.png"))); // NOI18N
        jLabel4.setText("  Dashboard");
        jLabel4.setToolTipText("");
        jLabel4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel4.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
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

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/BORROW LOGO.png"))); // NOI18N
        jLabel7.setText("  Borrow Books");
        jLabel7.setToolTipText("");
        jLabel7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel7.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel7MouseClicked(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/ADD STUDENTS.png"))); // NOI18N
        jLabel8.setText("  Add Students");
        jLabel8.setToolTipText("");
        jLabel8.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel8.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel8MouseClicked(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/RETURN LOGO.png"))); // NOI18N
        jLabel9.setText("Return Books");
        jLabel9.setToolTipText("");
        jLabel9.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel9.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel9.setIconTextGap(10);
        jLabel9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel9MouseClicked(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/RECORD.png"))); // NOI18N
        jLabel10.setText("Records");
        jLabel10.setToolTipText("");
        jLabel10.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel10.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel10.setIconTextGap(10);
        jLabel10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel10MouseClicked(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/ovedue.png"))); // NOI18N
        jLabel14.setText("Overdue Books");
        jLabel14.setToolTipText("");
        jLabel14.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel14.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel14.setIconTextGap(10);
        jLabel14.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel14MouseClicked(evt);
            }
        });

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/BOOKS.png"))); // NOI18N
        jLabel16.setText("Books");
        jLabel16.setToolTipText("");
        jLabel16.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel16.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel16.setIconTextGap(10);
        jLabel16.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel16MouseClicked(evt);
            }
        });

        jSeparator1.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator1.setForeground(new java.awt.Color(255, 255, 255));
        jSeparator1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));

        termscondition.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        termscondition.setForeground(new java.awt.Color(255, 255, 255));
        termscondition.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/teRMSANDcONDITION.png"))); // NOI18N
        termscondition.setText("Terms & Condition");
        termscondition.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        termscondition.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                termsconditionMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(termscondition, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 296, Short.MAX_VALUE)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(termscondition)
                .addGap(18, 18, 18))
        );

        getContentPane().add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 100, 270, 980));

        jTabbedPane1.setBackground(new java.awt.Color(54, 101, 145));

        Dashboard.setBackground(new java.awt.Color(54, 101, 145));
        Dashboard.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));

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

        jLabel30.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(255, 255, 255));
        jLabel30.setText("TIME NOW");

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
            .addComponent(date, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(date)
                .addContainerGap(44, Short.MAX_VALUE))
        );

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

        jLabel36.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel36.setForeground(new java.awt.Color(255, 255, 255));
        jLabel36.setText("No Of Students");

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
            .addComponent(time, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(time)
                .addContainerGap(44, Short.MAX_VALUE))
        );

        jLabel38.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel38.setForeground(new java.awt.Color(255, 255, 255));
        jLabel38.setText("DATE");

        studentdetails.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 0));
        studentdetails.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Student Id", "LRN", "Fullname", "Grade Lvl", "Section", "Contact"
            }
        ));
        studentdetails.setColorBackgoundHead(new java.awt.Color(54, 101, 145));
        studentdetails.setColorBordeFilas(new java.awt.Color(255, 255, 255));
        studentdetails.setColorBordeHead(new java.awt.Color(255, 255, 255));
        studentdetails.setColorFilasBackgound1(new java.awt.Color(54, 101, 145));
        studentdetails.setColorFilasBackgound2(new java.awt.Color(54, 101, 145));
        studentdetails.setColorFilasForeground1(new java.awt.Color(255, 255, 255));
        studentdetails.setColorFilasForeground2(new java.awt.Color(255, 255, 255));
        studentdetails.setColorSelBackgound(new java.awt.Color(54, 101, 145));
        studentdetails.setFuenteHead(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        studentdetails.setRowHeight(40);
        jScrollPane3.setViewportView(studentdetails);

        pPie.setPreferredSize(new java.awt.Dimension(540, 450));
        pPie.setLayout(new java.awt.BorderLayout());

        jLabel39.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel39.setForeground(new java.awt.Color(255, 255, 255));
        jLabel39.setText("Student Details");

        jLabel40.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel40.setForeground(new java.awt.Color(255, 255, 255));
        jLabel40.setText("No Of Books");

        javax.swing.GroupLayout DashboardLayout = new javax.swing.GroupLayout(Dashboard);
        Dashboard.setLayout(DashboardLayout);
        DashboardLayout.setHorizontalGroup(
            DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DashboardLayout.createSequentialGroup()
                .addGap(102, 102, 102)
                .addComponent(jLabel40)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 615, Short.MAX_VALUE)
                .addComponent(jLabel38)
                .addGap(345, 345, 345)
                .addComponent(jLabel30)
                .addGap(332, 332, 332))
            .addGroup(DashboardLayout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jLabel39)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(DashboardLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(DashboardLayout.createSequentialGroup()
                            .addGap(441, 441, 441)
                            .addComponent(jLabel36))
                        .addGroup(DashboardLayout.createSequentialGroup()
                            .addGap(76, 76, 76)
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(105, 105, 105)
                            .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(97, 97, 97)
                            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(85, 85, 85)
                            .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(DashboardLayout.createSequentialGroup()
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 1013, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(37, 37, 37)
                            .addComponent(pPie, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        DashboardLayout.setVerticalGroup(
            DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DashboardLayout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel40)
                    .addComponent(jLabel30)
                    .addComponent(jLabel38))
                .addGap(267, 267, 267)
                .addComponent(jLabel39)
                .addContainerGap(614, Short.MAX_VALUE))
            .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(DashboardLayout.createSequentialGroup()
                    .addGap(0, 42, Short.MAX_VALUE)
                    .addComponent(jLabel36)
                    .addGap(6, 6, 6)
                    .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(DashboardLayout.createSequentialGroup()
                            .addGap(174, 174, 174)
                            .addComponent(pPie, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, DashboardLayout.createSequentialGroup()
                            .addGap(166, 166, 166)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 458, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGap(0, 132, Short.MAX_VALUE)))
        );

        jTabbedPane1.addTab("tab2", Dashboard);

        addStudents.setBackground(new java.awt.Color(54, 101, 145));
        addStudents.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 50)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(255, 255, 255));
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("ADD STUDENTS");

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

        jLabel46.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel46.setForeground(new java.awt.Color(255, 255, 255));
        jLabel46.setText("LRN:");

        jLabel47.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel47.setForeground(new java.awt.Color(255, 255, 255));
        jLabel47.setText("FULLNAME:");

        jLabel48.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel48.setForeground(new java.awt.Color(255, 255, 255));
        jLabel48.setText("GRADE LVL:");

        jLabel49.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel49.setForeground(new java.awt.Color(255, 255, 255));
        jLabel49.setText("SECTION:");

        jScrollPane6.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        tstudent.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Lrn", "Fullname", "Grade lvl", "Section", "Contact Number", "Remarks"
            }
        ));
        tstudent.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tstudentMouseClicked(evt);
            }
        });
        jScrollPane6.setViewportView(tstudent);

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

        jLabel50.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel50.setForeground(new java.awt.Color(255, 255, 255));
        jLabel50.setText("CONTACT:");

        txtsection1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtsection1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout addStudentsLayout = new javax.swing.GroupLayout(addStudents);
        addStudents.setLayout(addStudentsLayout);
        addStudentsLayout.setHorizontalGroup(
            addStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addStudentsLayout.createSequentialGroup()
                .addGroup(addStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(addStudentsLayout.createSequentialGroup()
                        .addGroup(addStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(addStudentsLayout.createSequentialGroup()
                                .addGap(331, 331, 331)
                                .addGroup(addStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(addStudentsLayout.createSequentialGroup()
                                        .addComponent(jLabel47)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtfname, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(addStudentsLayout.createSequentialGroup()
                                        .addComponent(jLabel46, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtlrn, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(addStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(addStudentsLayout.createSequentialGroup()
                                        .addGap(116, 116, 116)
                                        .addGroup(addStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel49)
                                            .addComponent(jLabel48)))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addStudentsLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel50)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(addStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(txtsection1)
                                    .addComponent(txtgrade, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                                    .addComponent(txtsection, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)))
                            .addGroup(addStudentsLayout.createSequentialGroup()
                                .addGap(45, 45, 45)
                                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 1556, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 37, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(addStudentsLayout.createSequentialGroup()
                .addGap(637, 637, 637)
                .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        addStudentsLayout.setVerticalGroup(
            addStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addStudentsLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jLabel18)
                .addGap(45, 45, 45)
                .addGroup(addStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel48, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtgrade, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37)
                .addGroup(addStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtsection, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel49, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtlrn, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel46, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(44, 44, 44)
                .addGroup(addStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtsection1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel50, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(addStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtfname, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel47, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 111, Short.MAX_VALUE)
                .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(72, 72, 72)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 406, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        jTabbedPane1.addTab("tab4", addStudents);

        Books.setBackground(new java.awt.Color(54, 101, 145));
        Books.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));
        Books.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                BooksMouseClicked(evt);
            }
        });

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

        jLabel55.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel55.setForeground(new java.awt.Color(255, 255, 255));
        jLabel55.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/searchbutton.png"))); // NOI18N
        jLabel55.setText("SEARCH");

        jLabel56.setFont(new java.awt.Font("Segoe UI", 1, 50)); // NOI18N
        jLabel56.setForeground(new java.awt.Color(255, 255, 255));
        jLabel56.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel56.setText("BOOKS");

        javax.swing.GroupLayout BooksLayout = new javax.swing.GroupLayout(Books);
        Books.setLayout(BooksLayout);
        BooksLayout.setHorizontalGroup(
            BooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel56, javax.swing.GroupLayout.DEFAULT_SIZE, 1644, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, BooksLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(BooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(BooksLayout.createSequentialGroup()
                        .addComponent(jLabel55)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(searchh, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1572, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32))
        );
        BooksLayout.setVerticalGroup(
            BooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(BooksLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel56)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 178, Short.MAX_VALUE)
                .addGroup(BooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchh, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel55))
                .addGap(28, 28, 28)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 613, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25))
        );

        jTabbedPane1.addTab("tab8", Books);

        BookBorrow.setBackground(new java.awt.Color(54, 101, 145));
        BookBorrow.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));

        jPanel2.setBackground(new java.awt.Color(54, 101, 145));
        jPanel2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 4, true));
        jPanel2.setPreferredSize(new java.awt.Dimension(682, 1060));

        jSeparator2.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator2.setForeground(new java.awt.Color(255, 255, 255));
        jSeparator2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("TITLE:");
        jLabel11.setToolTipText("");

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
        jLabel17.setText("AUTHOR:");
        jLabel17.setToolTipText("");

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(255, 255, 255));
        jLabel19.setText("ISBN:");
        jLabel19.setToolTipText("");

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(255, 255, 255));
        jLabel20.setText("CATEGORY:");
        jLabel20.setToolTipText("");

        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(255, 255, 255));
        jLabel21.setText("QUANTITY:");
        jLabel21.setToolTipText("");

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(255, 255, 255));
        jLabel22.setText("PUBLISHED DATE:");
        jLabel22.setToolTipText("");

        lbltitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lbltitle.setForeground(new java.awt.Color(255, 255, 255));

        lblauthor.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblauthor.setForeground(new java.awt.Color(255, 255, 255));

        lblisbn.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblisbn.setForeground(new java.awt.Color(255, 255, 255));

        lblquantity.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblquantity.setForeground(new java.awt.Color(255, 255, 255));

        lblpublish.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblpublish.setForeground(new java.awt.Color(255, 255, 255));

        lblcategory.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblcategory.setForeground(new java.awt.Color(255, 255, 255));

        jLabel33.setFont(new java.awt.Font("Segoe UI", 0, 50)); // NOI18N
        jLabel33.setForeground(new java.awt.Color(255, 255, 255));
        jLabel33.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel33.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/open book.png"))); // NOI18N
        jLabel33.setText("BOOK DETAILS");
        jLabel33.setToolTipText("");

        bookerror.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        bookerror.setForeground(new java.awt.Color(255, 0, 0));
        bookerror.setToolTipText("");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel21, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel20))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblquantity, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblcategory, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel22)
                                .addGap(18, 18, 18)
                                .addComponent(lblpublish, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel19)
                                .addGap(18, 18, 18)
                                .addComponent(lblisbn, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel17)
                                .addGap(18, 18, 18)
                                .addComponent(lblauthor, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addGap(18, 18, 18)
                                .addComponent(lbltitle, javax.swing.GroupLayout.PREFERRED_SIZE, 405, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(bookerror, javax.swing.GroupLayout.PREFERRED_SIZE, 432, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(63, 63, 63)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 412, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(35, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel33, javax.swing.GroupLayout.DEFAULT_SIZE, 533, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(130, 130, 130)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(165, 165, 165)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(lbltitle, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(lblauthor, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(lblisbn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(lblcategory, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(lblquantity, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(lblpublish, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(54, 54, 54)
                .addComponent(bookerror, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(81, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(75, 75, 75)
                    .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(717, Short.MAX_VALUE)))
        );

        jPanel6.setBackground(new java.awt.Color(54, 101, 145));
        jPanel6.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 4, true));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 50)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/STUDENTSS.png"))); // NOI18N
        jLabel1.setText("STUDENT DETAILS");
        jLabel1.setToolTipText("");

        jSeparator3.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator3.setForeground(new java.awt.Color(255, 255, 255));
        jSeparator3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));

        jLabel35.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(255, 255, 255));
        jLabel35.setText("LRN:");
        jLabel35.setToolTipText("");

        jLabel42.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel42.setForeground(new java.awt.Color(255, 255, 255));
        jLabel42.setText("FULLNAME:");
        jLabel42.setToolTipText("");

        jLabel43.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel43.setForeground(new java.awt.Color(255, 255, 255));
        jLabel43.setText("GRADE LVL:");
        jLabel43.setToolTipText("");

        jLabel44.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel44.setForeground(new java.awt.Color(255, 255, 255));
        jLabel44.setText("SECTION:");
        jLabel44.setToolTipText("");

        jLabel51.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel51.setForeground(new java.awt.Color(255, 255, 255));
        jLabel51.setText("CONTACT NUMBER:");
        jLabel51.setToolTipText("");

        lblfname.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblfname.setForeground(new java.awt.Color(255, 255, 255));

        lblgrade.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblgrade.setForeground(new java.awt.Color(255, 255, 255));

        lblsec.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblsec.setForeground(new java.awt.Color(255, 255, 255));

        lblcontact.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblcontact.setForeground(new java.awt.Color(255, 255, 255));

        lbllrn.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lbllrn.setForeground(new java.awt.Color(255, 255, 255));

        studenterror.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        studenterror.setForeground(new java.awt.Color(255, 0, 0));
        studenterror.setToolTipText("");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(70, Short.MAX_VALUE)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 443, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45))
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(70, 70, 70)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(studenterror, javax.swing.GroupLayout.PREFERRED_SIZE, 432, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel44)
                        .addGap(18, 18, 18)
                        .addComponent(lblsec, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel43)
                        .addGap(18, 18, 18)
                        .addComponent(lblgrade, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel42)
                        .addGap(18, 18, 18)
                        .addComponent(lblfname, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel35)
                        .addGap(18, 18, 18)
                        .addComponent(lbllrn, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel51)
                        .addGap(18, 18, 18)
                        .addComponent(lblcontact, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(65, 65, 65)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(154, 154, 154)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel35)
                    .addComponent(lbllrn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(47, 47, 47)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel42)
                    .addComponent(lblfname, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(56, 56, 56)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel43)
                    .addComponent(lblgrade, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(49, 49, 49)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel44)
                    .addComponent(lblsec, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel51)
                    .addComponent(lblcontact, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(89, 89, 89)
                .addComponent(studenterror, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(93, Short.MAX_VALUE))
        );

        jSeparator4.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator4.setForeground(new java.awt.Color(255, 255, 255));
        jSeparator4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));

        jLabel23.setFont(new java.awt.Font("Segoe UI", 0, 36)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(255, 255, 255));
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/BOOKS LOGO.png"))); // NOI18N
        jLabel23.setText("BORROW BOOKS");
        jLabel23.setToolTipText("");

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(255, 255, 255));
        jLabel24.setText("ISBN:");

        txtbookid.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtbookidFocusLost(evt);
            }
        });

        txtstudentid.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtstudentidFocusLost(evt);
            }
        });

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(255, 255, 255));
        jLabel25.setText("LRN:");

        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(255, 255, 255));
        jLabel26.setText("ISSUE DATE:");

        jLabel27.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(255, 255, 255));
        jLabel27.setText("DUE DATE:");

        duedate.setPlaceholder("DUE DATE");

        issuedate.setPlaceholder("ISSUE DATE");

        bookborrows.setBackground(new java.awt.Color(54, 101, 145));
        bookborrows.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        bookborrows.setForeground(new java.awt.Color(255, 255, 255));
        bookborrows.setText("BORROW BOOK");
        bookborrows.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));
        bookborrows.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bookborrowsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout BookBorrowLayout = new javax.swing.GroupLayout(BookBorrow);
        BookBorrow.setLayout(BookBorrowLayout);
        BookBorrowLayout.setHorizontalGroup(
            BookBorrowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(BookBorrowLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 553, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(BookBorrowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(BookBorrowLayout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, BookBorrowLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(BookBorrowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, BookBorrowLayout.createSequentialGroup()
                                .addGroup(BookBorrowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel26)
                                    .addComponent(jLabel24)
                                    .addComponent(txtbookid, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                                    .addComponent(jSeparator4, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                                    .addComponent(jLabel25)
                                    .addComponent(txtstudentid)
                                    .addComponent(issuedate, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                                    .addComponent(jLabel27)
                                    .addComponent(duedate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(31, 31, 31))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, BookBorrowLayout.createSequentialGroup()
                                .addComponent(bookborrows, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(125, 125, 125))))))
        );
        BookBorrowLayout.setVerticalGroup(
            BookBorrowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(BookBorrowLayout.createSequentialGroup()
                .addGroup(BookBorrowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(BookBorrowLayout.createSequentialGroup()
                        .addGap(75, 75, 75)
                        .addGroup(BookBorrowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 847, Short.MAX_VALUE)
                            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(BookBorrowLayout.createSequentialGroup()
                        .addGap(158, 158, 158)
                        .addComponent(jLabel23)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(98, 98, 98)
                        .addComponent(jLabel24)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtbookid, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(64, 64, 64)
                        .addComponent(jLabel25)
                        .addGap(18, 18, 18)
                        .addComponent(txtstudentid, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(44, 44, 44)
                        .addComponent(jLabel26)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(issuedate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40)
                        .addComponent(jLabel27)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(duedate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(bookborrows, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("tab8", BookBorrow);

        ReturnBooks.setBackground(new java.awt.Color(54, 101, 145));
        ReturnBooks.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));

        jPanel7.setBackground(new java.awt.Color(54, 101, 145));
        jPanel7.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 4, true));
        jPanel7.setPreferredSize(new java.awt.Dimension(682, 1060));

        jSeparator5.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator5.setForeground(new java.awt.Color(255, 255, 255));
        jSeparator5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));

        jLabel31.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel31.setForeground(new java.awt.Color(255, 255, 255));
        jLabel31.setText("STUDENT NAME:");
        jLabel31.setToolTipText("");

        jLabel45.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel45.setForeground(new java.awt.Color(255, 255, 255));
        jLabel45.setText("TITLE:");
        jLabel45.setToolTipText("");

        jLabel52.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel52.setForeground(new java.awt.Color(255, 255, 255));
        jLabel52.setText("LRN:");
        jLabel52.setToolTipText("");

        jLabel53.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel53.setForeground(new java.awt.Color(255, 255, 255));
        jLabel53.setText("ISSUE DATE:");
        jLabel53.setToolTipText("");

        jLabel54.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel54.setForeground(new java.awt.Color(255, 255, 255));
        jLabel54.setText("DUE DATE:");
        jLabel54.setToolTipText("");

        lbltitles.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lbltitles.setForeground(new java.awt.Color(255, 255, 255));

        lblstudent.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblstudent.setForeground(new java.awt.Color(255, 255, 255));

        lblissuedate.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblissuedate.setForeground(new java.awt.Color(255, 255, 255));

        lblissueid.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblissueid.setForeground(new java.awt.Color(255, 255, 255));

        lblduedate.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblduedate.setForeground(new java.awt.Color(255, 255, 255));

        jLabel57.setFont(new java.awt.Font("Segoe UI", 0, 50)); // NOI18N
        jLabel57.setForeground(new java.awt.Color(255, 255, 255));
        jLabel57.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel57.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/open book.png"))); // NOI18N
        jLabel57.setText("BOOK DETAILS");
        jLabel57.setToolTipText("");

        bookerror1.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        bookerror1.setForeground(new java.awt.Color(255, 0, 0));
        bookerror1.setToolTipText("");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel52)
                                .addGap(18, 18, 18)
                                .addComponent(lblstudent, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel31)
                                .addGap(18, 18, 18)
                                .addComponent(lblissueid, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel45)
                                .addGap(18, 18, 18)
                                .addComponent(lbltitles, javax.swing.GroupLayout.PREFERRED_SIZE, 411, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel53)
                                .addGap(18, 18, 18)
                                .addComponent(lblissuedate, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel54)
                                .addGap(18, 18, 18)
                                .addComponent(lblduedate, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(bookerror1, javax.swing.GroupLayout.PREFERRED_SIZE, 432, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(63, 63, 63)
                        .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 412, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel57, javax.swing.GroupLayout.PREFERRED_SIZE, 537, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(306, Short.MAX_VALUE)))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(130, 130, 130)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(86, 86, 86)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31)
                    .addComponent(lblissueid, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(47, 47, 47)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel45)
                    .addComponent(lbltitles, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel52)
                    .addComponent(lblstudent, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel53)
                    .addComponent(lblissuedate, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel54)
                    .addComponent(lblduedate, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(73, 73, 73)
                .addComponent(bookerror1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addGap(75, 75, 75)
                    .addComponent(jLabel57, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(717, Short.MAX_VALUE)))
        );

        returnbook.setBackground(new java.awt.Color(54, 101, 145));
        returnbook.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        returnbook.setForeground(new java.awt.Color(255, 255, 255));
        returnbook.setText("RETURN BOOKS");
        returnbook.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));
        returnbook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                returnbookActionPerformed(evt);
            }
        });

        jSeparator6.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator6.setForeground(new java.awt.Color(255, 255, 255));
        jSeparator6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));

        jLabel58.setFont(new java.awt.Font("Segoe UI", 0, 36)); // NOI18N
        jLabel58.setForeground(new java.awt.Color(255, 255, 255));
        jLabel58.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel58.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/BOOKS LOGO.png"))); // NOI18N
        jLabel58.setText("RETURN BOOKS");
        jLabel58.setToolTipText("");

        booksid.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                booksidFocusLost(evt);
            }
        });

        studentsid.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                studentsidFocusLost(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jLabel59.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel59.setForeground(new java.awt.Color(255, 255, 255));
        jLabel59.setText("LRN:");

        jLabel60.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel60.setForeground(new java.awt.Color(255, 255, 255));
        jLabel60.setText("ISBN:");

        finddetails.setBackground(new java.awt.Color(54, 101, 145));
        finddetails.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        finddetails.setForeground(new java.awt.Color(255, 255, 255));
        finddetails.setText("FIND DETAILS");
        finddetails.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));
        finddetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finddetailsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ReturnBooksLayout = new javax.swing.GroupLayout(ReturnBooks);
        ReturnBooks.setLayout(ReturnBooksLayout);
        ReturnBooksLayout.setHorizontalGroup(
            ReturnBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ReturnBooksLayout.createSequentialGroup()
                .addGap(199, 199, 199)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 553, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(ReturnBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ReturnBooksLayout.createSequentialGroup()
                        .addGap(152, 152, 152)
                        .addComponent(jLabel58, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ReturnBooksLayout.createSequentialGroup()
                        .addGap(171, 171, 171)
                        .addGroup(ReturnBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12)
                            .addGroup(ReturnBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(booksid, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                                .addComponent(jSeparator6)
                                .addComponent(studentsid)
                                .addComponent(returnbook, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(finddetails, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jLabel59)
                            .addComponent(jLabel60))
                        .addGap(25, 25, 25)))
                .addGap(300, 300, 300))
        );
        ReturnBooksLayout.setVerticalGroup(
            ReturnBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ReturnBooksLayout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 847, Short.MAX_VALUE)
                .addGap(62, 62, 62))
            .addGroup(ReturnBooksLayout.createSequentialGroup()
                .addGap(143, 143, 143)
                .addComponent(jLabel58)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(90, 90, 90)
                .addComponent(jLabel12)
                .addGap(12, 12, 12)
                .addComponent(jLabel60)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(booksid, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jLabel59)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(studentsid, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(232, 232, 232)
                .addComponent(finddetails, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(returnbook, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(165, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("tab5", ReturnBooks);

        Record.setBackground(new java.awt.Color(54, 101, 145));
        Record.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));

        jSeparator7.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator7.setForeground(new java.awt.Color(255, 255, 255));
        jSeparator7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));

        jLabel62.setFont(new java.awt.Font("Segoe UI", 0, 55)); // NOI18N
        jLabel62.setForeground(new java.awt.Color(255, 255, 255));
        jLabel62.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel62.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/open book.png"))); // NOI18N
        jLabel62.setText("VIEW RECORDS");
        jLabel62.setToolTipText("");

        trecords.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        trecords.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane7.setViewportView(trecords);

        jPanel14.setBackground(new java.awt.Color(54, 101, 145));
        jPanel14.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));
        jPanel14.setPreferredSize(new java.awt.Dimension(1100, 101));

        DUEDATES.setPlaceholder("DUE DATE");

        ISSUEDATE.setPlaceholder("ISSUE DATE");

        jLabel28.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(255, 255, 255));
        jLabel28.setText("ISSUE DATE:");

        jLabel61.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel61.setForeground(new java.awt.Color(255, 255, 255));
        jLabel61.setText("DUE DATE:");

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
                .addComponent(jLabel61)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(DUEDATES, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(SEARCHH, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(382, Short.MAX_VALUE))
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
                .addComponent(jLabel61, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36))
        );

        javax.swing.GroupLayout RecordLayout = new javax.swing.GroupLayout(Record);
        Record.setLayout(RecordLayout);
        RecordLayout.setHorizontalGroup(
            RecordLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RecordLayout.createSequentialGroup()
                .addGroup(RecordLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(RecordLayout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 1585, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, 1643, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 1, Short.MAX_VALUE))
            .addGroup(RecordLayout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jLabel62, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RecordLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, 470, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(583, 583, 583))
        );
        RecordLayout.setVerticalGroup(
            RecordLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RecordLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel62, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46)
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(148, 148, 148)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 516, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45))
        );

        jTabbedPane1.addTab("tab6", Record);

        BookIssues.setBackground(new java.awt.Color(54, 101, 145));
        BookIssues.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));

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

        javax.swing.GroupLayout BookIssuesLayout = new javax.swing.GroupLayout(BookIssues);
        BookIssues.setLayout(BookIssuesLayout);
        BookIssuesLayout.setHorizontalGroup(
            BookIssuesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(BookIssuesLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 1585, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(35, Short.MAX_VALUE))
        );
        BookIssuesLayout.setVerticalGroup(
            BookIssuesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, BookIssuesLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 907, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29))
        );

        jTabbedPane1.addTab("tab7", BookIssues);

        jPanel8.setBackground(new java.awt.Color(54, 101, 145));

        jLabel63.setFont(new java.awt.Font("Segoe UI", 0, 55)); // NOI18N
        jLabel63.setForeground(new java.awt.Color(255, 255, 255));
        jLabel63.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel63.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images.Homepage/open book.png"))); // NOI18N
        jLabel63.setText("BORROWED BOOKS");
        jLabel63.setToolTipText("");

        trecords1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        trecords1.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane8.setViewportView(trecords1);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel63, javax.swing.GroupLayout.DEFAULT_SIZE, 1589, Short.MAX_VALUE)
                .addGap(55, 55, 55))
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addGap(32, 32, 32)
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 1585, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(33, Short.MAX_VALUE)))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addComponent(jLabel63, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(883, Short.MAX_VALUE))
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addGap(240, 240, 240)
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 650, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jTabbedPane1.addTab("tab8", jPanel8);

        getContentPane().add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 71, 1650, 1010));

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

    private void jLabel8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel8MouseClicked
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(1);
        refreshdata();
    }//GEN-LAST:event_jLabel8MouseClicked

    private void jLabel7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MouseClicked
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(3);
        refreshdata();
    }//GEN-LAST:event_jLabel7MouseClicked

    private void jLabel9MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel9MouseClicked
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(4);
        refreshdata();
    }//GEN-LAST:event_jLabel9MouseClicked

    private void jLabel10MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel10MouseClicked
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(5);
        refreshdata();
    }//GEN-LAST:event_jLabel10MouseClicked

    private void jLabel14MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel14MouseClicked
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(6);
        refreshdata();
        
    }//GEN-LAST:event_jLabel14MouseClicked

    private void jLabel16MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel16MouseClicked
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(2);
        refreshdata();
    }//GEN-LAST:event_jLabel16MouseClicked

    private void BooksMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_BooksMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_BooksMouseClicked

    private void txtgradeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtgradeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtgradeActionPerformed

    private void txtsectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtsectionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtsectionActionPerformed

    private void txtfnameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtfnameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtfnameActionPerformed

    private void tstudentMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tstudentMouseClicked
        // TODO add your handling code here:
      
    }//GEN-LAST:event_tstudentMouseClicked

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        // TODO add your handling code here:
  
        try {
                 String lrn = txtlrn.getText().trim();
                 String fullname = txtfname.getText().trim();
                 String gradelvl = txtgrade.getText().trim();
                 String section = txtsection.getText().trim();
                 String contact = txtsection1.getText().trim();



                 if (lrn.isEmpty() || fullname.isEmpty() || gradelvl.isEmpty() || section.isEmpty() || contact.isEmpty()) {
                     JOptionPane.showMessageDialog(rootPane, "No input! Please fill in all fields.");
                     return;
                 }





                 con = LibManSys.getConnection();


                 String insertQuery = "INSERT INTO student (lrn, fullname, grade_lvl, section, contact_number) VALUES (?, ?, ?, ?, ?)";
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

    private void termsconditionMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_termsconditionMouseClicked
        // TODO add your handling code here:
        showTermsAndConditions();
    }//GEN-LAST:event_termsconditionMouseClicked

    private void txtbookidFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtbookidFocusLost
        // TODO add your handling code here:
               if (!txtbookid.getText().equals("")) {
            getbooks();
}
    }//GEN-LAST:event_txtbookidFocusLost

    private void txtstudentidFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtstudentidFocusLost
        // TODO add your handling code here:
       if (!txtstudentid.getText().equals("")) {
            getstudent();
}
    }//GEN-LAST:event_txtstudentidFocusLost

    private void bookborrowsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bookborrowsActionPerformed
        // TODO add your handling code here:
        if (issuebook()) {
            JOptionPane.showMessageDialog(null, "Book Issued Successfully!");
            
        } else {
            JOptionPane.showMessageDialog(null, "Failed to Issue the Book.");
        }
     
    }//GEN-LAST:event_bookborrowsActionPerformed

    private void returnbookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_returnbookActionPerformed
        // TODO add your handling code here:
  returnBookandupdatequantity();
    }//GEN-LAST:event_returnbookActionPerformed

    private void booksidFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_booksidFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_booksidFocusLost

    private void studentsidFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_studentsidFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_studentsidFocusLost

    private void finddetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_finddetailsActionPerformed
        // TODO add your handling code here:
        getIssuebookdetails();
    }//GEN-LAST:event_finddetailsActionPerformed

    private void tbooksMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbooksMouseClicked
        // TODO add your handling code here:
        
    }//GEN-LAST:event_tbooksMouseClicked

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

    private void txtsection1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtsection1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtsection1ActionPerformed

    private void SEARCHHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SEARCHHActionPerformed
        // TODO add your handling code here:
        searchh();
    }//GEN-LAST:event_SEARCHHActionPerformed

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
            java.util.logging.Logger.getLogger(LibUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LibUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LibUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LibUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LibUser().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel BookBorrow;
    private javax.swing.JPanel BookIssues;
    private javax.swing.JPanel Books;
    private rojeru_san.componentes.RSDateChooser DUEDATES;
    private javax.swing.JPanel Dashboard;
    private rojeru_san.componentes.RSDateChooser ISSUEDATE;
    private javax.swing.JPanel Record;
    private javax.swing.JPanel ReturnBooks;
    private javax.swing.JButton SEARCHH;
    private javax.swing.JPanel addStudents;
    private javax.swing.JButton bookborrows;
    private javax.swing.JLabel bookerror;
    private javax.swing.JLabel bookerror1;
    private javax.swing.JTextField booksid;
    private javax.swing.JLabel date;
    private rojeru_san.componentes.RSDateChooser duedate;
    private javax.swing.JButton finddetails;
    private rojeru_san.componentes.RSDateChooser issuedate;
    private javax.swing.JButton jButton10;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblauthor;
    private javax.swing.JLabel lblcategory;
    private javax.swing.JLabel lblcontact;
    private javax.swing.JLabel lblduedate;
    private javax.swing.JLabel lblfname;
    private javax.swing.JLabel lblgrade;
    private javax.swing.JLabel lblisbn;
    private javax.swing.JLabel lblissuedate;
    private javax.swing.JLabel lblissueid;
    private javax.swing.JLabel lbllrn;
    private javax.swing.JLabel lblpublish;
    private javax.swing.JLabel lblquantity;
    private javax.swing.JLabel lblsec;
    private javax.swing.JLabel lblstudent;
    private javax.swing.JLabel lbltitle;
    private javax.swing.JLabel lbltitles;
    private javax.swing.JLabel nobooks;
    private javax.swing.JLabel nostudents;
    private javax.swing.JPanel pPie;
    private javax.swing.JButton returnbook;
    private javax.swing.JTextField searchh;
    private rojerusan.RSTableMetro studentdetails;
    private javax.swing.JLabel studenterror;
    private javax.swing.JTextField studentsid;
    private javax.swing.JTable tbooks;
    private javax.swing.JLabel termscondition;
    private javax.swing.JLabel time;
    private javax.swing.JTable trecords;
    private javax.swing.JTable trecords1;
    private javax.swing.JTable trecordsss;
    private javax.swing.JTable tstudent;
    private javax.swing.JTextField txtbookid;
    private javax.swing.JTextField txtfname;
    private javax.swing.JTextField txtgrade;
    private javax.swing.JTextField txtlrn;
    private javax.swing.JTextField txtsection;
    private javax.swing.JTextField txtsection1;
    private javax.swing.JTextField txtstudentid;
    // End of variables declaration//GEN-END:variables
}
