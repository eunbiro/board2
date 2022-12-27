package Controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;

import DAO.BoardDAO;
import DTO.Board;

@WebServlet("/")
public class BoardController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private BoardDAO dao;
	private ServletContext ctx;
	
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		// init은 서블릿 객체 생성 시 딱 한번만 실행되므로 객체를 한번만 생성해 공유할 수 있다. 
		dao = new BoardDAO();
		ctx = getServletContext();		// 웹 어블리케이션의 자원 관리
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		doPro(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		doPro(request, response);
	}
	
	protected void doPro(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String context = request.getContextPath();
		String command = request.getServletPath();
		String site = null;
		
			// 경로 라우팅(경로를 찾아줌)
		switch(command) {
		case "/list":
			site = getList(request);
			break;
		case "/view":
			site = getView(request);
			break;
		case "/write":
			site = "write.jsp";
			break;
		case "/insert":
			site = insertBoard(request);
			break;
		case "/edit":
			site = getViewForEdit(request);
			break;
		case "/update":
			site = updateBoard(request);
			break;
		case "/delete":
			site = deleteBoard(request);
			break;
		}
		
		/*
			redirect : URL의 변화가 있음 객체의 재사용이 불가 (request, response객체)
				*DB에 변화가 생기는 요청에 사용(글쓰기, 회원가입...)
			forward  : URL의 변화가 없음 (보안 등의 이유), 객체의 재사용이 가능 (request, response객체)
				*단순 조회(list, 검색..., select)
		*/
		
		if (site.startsWith("redirect:/")) {		// startsWith : redirect 문자열로 시작하는 것을 찾음
			String rview = site.substring("redirect:/".length());
			System.out.println(rview);
			response.sendRedirect(rview);
			
		} else {									// forward
			ctx.getRequestDispatcher("/" + site)
			.forward(request, response);
		}
	}
	
	public String getList(HttpServletRequest request) {
		List<Board> list;
		
		try {
			list = dao.getList();
			request.setAttribute("boardList", list);
			
		} catch (Exception e) {
			e.printStackTrace();
			ctx.log("게시판 목록 생성 과정에서 문제 발생");
			request.setAttribute("error", "게시판 목록이 정상적으로 처리되지 않았습니다!");
								// 나중에 사용자에게 에러메시지를 보여주기 위해 저장
		}
		
		return "index.jsp";
	}
	
	public String getView(HttpServletRequest request) {
		int board_no = Integer.parseInt(request.getParameter("board_no"));
		Board board;
		
		try {
			dao.updateViews(board_no);				// 조회수 증가
			board = dao.getView(board_no);
			request.setAttribute("board", board);
			
		} catch (Exception e) {
			e.printStackTrace();
			ctx.log("게시글을 가져오는 과정에서 문제 발생");
			request.setAttribute("error", "게시글이 정상적으로 처리되지 않았습니다!");
			// 나중에 사용자에게 에러메시지를 보여주기 위해 저장
		}
		
		return "view.jsp";
	}

	public String insertBoard(HttpServletRequest request) {
		Board b = new Board();
		try {
			BeanUtils.populate(b, request.getParameterMap());
			dao.insertBoard(b);
			
		} catch (Exception e) {
			e.printStackTrace();
			ctx.log("게시글을 작성하는 과정에서 문제 발생");
			
			try {
					// get방식으로 넘겨줄 때 한글 깨짐을 방지한다.
				String encodeName = URLEncoder.encode("게시글이 정상적으로 등록되지 않았습니다!", "UTF-8");
				return "redirect:/list?error=" + encodeName;
				
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
//			request.setAttribute("error", "게시글을 정상적으로 등록되지 않았습니다!");
//			return getList(request);
		}
		
		return "redirect:/list";
	}
	
	
	public String getViewForEdit(HttpServletRequest request) {
		int board_no = Integer.parseInt(request.getParameter("board_no"));
		Board board;
		
		try {
			board = dao.getViewForEdit(board_no);
			request.setAttribute("board", board);
			
		} catch (Exception e) {
			e.printStackTrace();
			ctx.log("게시글을 가져오는 과정에서 문제 발생");
			request.setAttribute("error", "게시글이 정상적으로 처리되지 않았습니다!");
			// 나중에 사용자에게 에러메시지를 보여주기 위해 저장
		}
		
		return "edit.jsp";
	}
	
	public String updateBoard(HttpServletRequest request) {
		Board b = new Board();
		try {
			BeanUtils.populate(b, request.getParameterMap());
			dao.updateBoard(b);
			
		} catch (Exception e) {
			e.printStackTrace();
			ctx.log("게시글을 수정하는 과정에서 문제 발생");
			
			try {
					// get방식으로 넘겨줄 때 한글 깨짐을 방지한다.
				String encodeName = URLEncoder.encode("게시글이 정상적으로 수정되지 않았습니다!", "UTF-8");
				return "redirect:/view?board_no=" + b.getBoard_no() + "&error=" + encodeName;
				
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}
		
		return "redirect:/view?board_no=" + b.getBoard_no();
	}
	
	public String deleteBoard(HttpServletRequest request) {
		int board_no = Integer.parseInt(request.getParameter("board_no"));
		
		try {
			dao.deleltBoard(board_no);
			
		} catch (Exception e) {
			e.printStackTrace();
			ctx.log("게시글을 삭제하는 과정에서 문제 발생");
			
			try {
					// get방식으로 넘겨줄 때 한글 깨짐을 방지한다.
				String encodeName = URLEncoder.encode("게시글이 정상적으로 삭제되지 않았습니다!", "UTF-8");
				return "redirect:/list?error=" + encodeName;
				
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}
		
		return "redirect:/list";
	}
}
