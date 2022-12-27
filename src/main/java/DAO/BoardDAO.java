package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import DTO.Board;

public class BoardDAO {
	final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
	final String JDBC_URL = "jdbc:oracle:thin:@localhost:1521:xe";
	
	
		// 데이터베이스와의 연결 수행 메소드
	public Connection open() {
		Connection conn = null;
		try {
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(JDBC_URL, "test", "test1234");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return conn;		// 데이터베이스의 연결 객체를 리턴
	}
	
	public ArrayList<Board> getList() throws Exception {
		Connection conn = open();
		ArrayList<Board> boardList = new ArrayList<Board>();
		PreparedStatement pstmt = conn.prepareStatement("SELECT BOARD_NO, TITLE, USER_ID, TO_CHAR(REG_DATE, 'YYYY.MM.DD') REG_DATE, VIEWS FROM BOARD ORDER BY BOARD_NO DESC");
															// 쿼리문 등록 (컴파일)
		ResultSet rs = pstmt.executeQuery();			// 데이터베이스 결과 저장
			
			// 리소스 자동 닫기(try-with-resource)
		try (conn; pstmt; rs) {							// close()를 따로 안써줘도 됨
			while(rs.next()) {
				Board board = new Board();

				board.setBoard_no(rs.getInt(1));
				board.setTitle(rs.getString(2));
				board.setUser_id(rs.getString(3));
				board.setReg_date(rs.getString(4));
				board.setViews(rs.getInt(5));

				boardList.add(board);
			}
			return boardList;
		}
	}
	
	public Board getView(int board_no) throws Exception {
		Connection conn = open();
		Board b = new Board();
		PreparedStatement pstmt = conn.prepareStatement("SELECT BOARD_NO, TITLE, USER_ID, TO_CHAR(REG_DATE, 'YYYY.MM.DD') REG_DATE, VIEWS, CONTENT "
														+ "FROM BOARD WHERE BOARD_NO = ?");
		pstmt.setInt(1, board_no);
		ResultSet rs = pstmt.executeQuery();
		
		// 리소스 자동 닫기(try-with-resource)
		try (conn; pstmt; rs) {
			if (rs.next()) {
				
				b.setBoard_no(rs.getInt(1));
				b.setTitle(rs.getString(2));
				b.setUser_id(rs.getString(3));
				b.setReg_date(rs.getString(4));
				b.setViews(rs.getInt(5));
				b.setContent(rs.getString(6));
			}
			return b;
		}
	}
	
	public void updateViews(int board_no) throws Exception {
		Connection conn = open();
		PreparedStatement pstmt = conn.prepareStatement("UPDATE BOARD SET VIEWS = (VIEWS + 1) WHERE BOARD_NO = ?");
		
		try (conn; pstmt) {
			pstmt.setInt(1, board_no);
			pstmt.executeUpdate();
		}
	}
	
		// 게시판 글 등록
	public void insertBoard(Board b) throws Exception {
		Connection conn = open();
		PreparedStatement pstmt = conn.prepareStatement("INSERT INTO BOARD (BOARD_NO, USER_ID, TITLE, CONTENT, REG_DATE, VIEWS) "
														+ "VALUES(BOARD_SEQ.nextval, ?, ?, ?, sysdate, 0)");
		
		try (conn; pstmt) {
			pstmt.setString(1, b.getUser_id());
			pstmt.setString(2, b.getTitle());
			pstmt.setString(3, b.getContent());
			
			pstmt.executeUpdate();
		}
	}
	
	public Board getViewForEdit(int board_no) throws Exception {
		Connection conn = open();
		Board b = new Board();
		PreparedStatement pstmt = conn.prepareStatement("SELECT BOARD_NO, TITLE, USER_ID, CONTENT "
														+ "FROM BOARD WHERE BOARD_NO = ?");
		pstmt.setInt(1, board_no);
		ResultSet rs = pstmt.executeQuery();
		
		// 리소스 자동 닫기(try-with-resource)
		try (conn; pstmt; rs) {
			if (rs.next()) {
				
				b.setBoard_no(rs.getInt(1));
				b.setTitle(rs.getString(2));
				b.setUser_id(rs.getString(3));
				b.setContent(rs.getString(4));
			}
			return b;
		}
	}
	
	public void updateBoard(Board b) throws Exception {
		Connection conn = open();
		PreparedStatement pstmt = conn.prepareStatement("UPDATE BOARD SET USER_ID = ?, TITLE = ?, CONTENT = ? "
														+ "WHERE BOARD_NO = ?");
		
		try (conn; pstmt) {
			pstmt.setString(1, b.getUser_id());
			pstmt.setString(2, b.getTitle());
			pstmt.setString(3, b.getContent());
			pstmt.setInt(4, b.getBoard_no());
			
			pstmt.executeUpdate();
			
			if (pstmt.executeUpdate() != 1) {
				throw new Exception("수정된 글이 없습니다.");
			}
		}
	}
	
	public void deleltBoard(int board_no) throws Exception {
		Connection conn = open();
		PreparedStatement pstmt = conn.prepareStatement("DELETE FROM BOARD WHERE BOARD_NO = ?");
		
		try (conn; pstmt) {
			pstmt.setInt(1, board_no);
			
				// 삭제된 글이 없을 경우
			if (pstmt.executeUpdate() != 1) {
				throw new Exception("삭제된 글이 없습니다.");
			}
		}
	}
}
