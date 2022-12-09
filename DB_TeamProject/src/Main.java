import java.sql.*;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws SQLException {
		
		String url = "jdbc:postgresql://localhost:5432/postgres";
		String user = "postgres";
		String password = "xasa0718!";
		
		
		try
        {
			Statement st = null;
			ResultSet rs = null;
			Connection connect = null;
			
			Scanner scan = new Scanner(System.in);
			int input = 0;
			String id = "";
			
            System.out.println("Connecting PostgreSQL database");
            connect = DriverManager.getConnection(url, user, password);
            st = connect.createStatement();
            
            if(connect != null) {
            	
            	/* 5명 이상, 같은 사람이 같은 책 여러번 예약 못하도록 TRIGGER 설정 */
    			/*String T1 = "CREATE OR REPLACE FUNCTION self_ignore() returns trigger\r\n"
    					+ "AS $$ DECLARE BEGIN IF exists(select n_id from n_res where n_id = new.n_id and u_id = '"+ id + "') THEN\r\n"
    					+ "return null; ELSE return New; END IF;\r\n"
    					+ "END; $$ LANGUAGE 'plpgsql'; create trigger T1\r\n"
    					+ "before insert on n_res for each row\r\n"
    					+ "execute procedure self_ignore();"; 

    			st.executeUpdate(T1);

    			String T2 = "CREATE OR REPLACE FUNCTION full_ignore() returns trigger\r\n"
    					+ "AS $$ DECLARE BEGIN IF (select count(*) from n_res where n_id = new.n_id) > 5 THEN\r\n"
    					+ "return null; ELSE return New; END IF;\r\n"
    					+ "END; $$ LANGUAGE 'plpgsql'; create trigger T2\r\n"
    					+ "before insert on n_res for each row\r\n"
    					+ "execute procedure full_ignore();"; 

    			st.executeUpdate(T2);*/
            	
            	
            	System.out.println("1. 로그인, 2. 회원가입");
            	input = scan.nextInt();
            	
            	if(input == 1) {
            		id = login(scan, st, rs);
            		if(id.isEmpty()) {
            			return;
            		}
            	}
            	else if(input == 2) {
            		register(scan, st, rs);
            		System.out.println("로그인을 진행하겠습니다.");
            		id = login(scan, st, rs);
            		if(id.isEmpty()) {
            			return;
            		}
            	}
            	else {
            		System.out.println("올바르지 않은 접근입니다.");
            		return;
            	}
            	
            	while(true) {
            		System.out.println("0. 종료 , 1. 도서 검색 , 2. 도서 대여 , 3. 도서 반납 , 4. 신규 도서 조회 , 5. 신규 도서 예약 , 6. 맞춤 도서 선택");
            		input = scan.nextInt();
            		if(input == 0) {
            			break;
            		}
            		else if(input == 1) {
            			BookSearch(scan, st, rs);
            		}
            		else if(input == 2) {
            			BookLent(id, scan, st, rs);
            		}
            		else if(input == 3) {
            			BookReturn(id, scan, st, rs);
            		}
            		else if(input == 4) {
            			NewBookInquiry(scan, st, rs);
            		}
            		else if(input == 5) {
            			NewReservation(id, scan, st, rs);
            		}
            		else if(input == 6) {
            			SelectCustomBook(scan, st, rs);
            		}
            		else {
            			System.out.println("올바르지 않은 접근입니다.");
            			continue;
            		}
            	}
                if(rs != null)
                	rs.close();
                if(st != null)
                	st.close();
                if(connect != null)
                	connect.close();
            }
            else {
                throw new SQLException("Connection fail");
            }
        } catch (SQLException ex) {
            throw ex;
        }
	}
	
	public static String login(Scanner sc, Statement st, ResultSet rs) throws SQLException {
		sc.nextLine();
		System.out.println("아이디를 입력하세요");
		String id = sc.nextLine();
		System.out.println("패스워드를 입력하세요");
		String pw = sc.nextLine();
		String query = "";
		query = "select u_id from users where u_id = '" + id + "' and pw = '" + pw + "';";
		rs = st.executeQuery(query);
		if(rs.next()) {
			System.out.println("로그인 성공");
			return rs.getString("u_id");
		}
		System.out.println("로그인 실패");
		return "";
	}
	
	public static void register(Scanner sc, Statement st, ResultSet rs) throws SQLException {
		sc.nextLine();
		System.out.println("아이디를 입력하세요");
		String id = sc.nextLine();
		String query = "";
		query = "select u_id from users where u_id = '" + id + "';";
		rs = st.executeQuery(query);
		if(rs.next()) {
			System.out.println("중복된 아이디가 있습니다");
			register(sc, st, rs);
		}
		else {
			System.out.println("패스워드를 입력하세요");
			String pw = sc.nextLine();
			query = "insert into users values('" + id + "', '" + pw + "');";
			st.executeUpdate(query);
			System.out.println("회원가입이 완료되었습니다");
		}
		return;
	}
	
	public static void BookSearch(Scanner sc, Statement st, ResultSet rs) throws SQLException {
		sc.nextLine();
		System.out.println("찾고 싶은 도서 혹은 저자를 입력하세요.");
		String keyword = sc.nextLine();
		String query = "";
		query = "select distinct b_id, bName, wName, byear from Book where bName like '%" + keyword
				+ "%' or wName like '%" + keyword + "%';";
		rs = st.executeQuery(query);

		int num = 0;
		while (rs.next()) {
			int b_id = rs.getInt("b_id");
			String bName = rs.getString("bName");
			String wName = rs.getString("wName");
			int byear = rs.getInt("byear");
			num++;
			System.out.println(
					num + "\tBook ID: " + b_id + "  | 도서: " + bName + "   | 저자: " + wName + "   | 발행년도: " + byear);
		}
	}
	
	public static void BookLent(String id, Scanner sc, Statement st, ResultSet rs) throws SQLException {
		sc.nextLine();
		System.out.println("대여할 도서의 Book ID를 입력하세요.");
		int b_id = sc.nextInt();
		String query = "";
		query = "select b_id, bName, isRes from Book where b_id = '" + b_id + "';";
		rs = st.executeQuery(query);
		if (rs.next()) {
			String bName = rs.getString("bName");
			boolean isRes = rs.getBoolean("isRes");

			if (isRes == false) {
				st.executeUpdate("update Book set isRes = true where b_id = '" + b_id + "';");
				st.executeUpdate("insert into res values(default, '" + id + "', '" + b_id + "', '" + bName + "');");
				System.out.println("대여가 완료되었습니다.");
			} else
				System.out.println("이미 대여 중인 도서입니다.");
		}
	}
	
	public static void BookReturn(String id, Scanner sc, Statement st, ResultSet rs) throws SQLException {
		int cnt = 0;
		rs = st.executeQuery("select count(*) from res where u_id = '" + id + "';");
		while (rs.next()) {
			cnt = rs.getInt("count");
		}
		if (cnt == 0) {
			System.out.println("반납할 도서가 없습니다.");
		}
		else {
			rs = st.executeQuery("select u_id, b_id, bName from res where u_id = '" + id + "' order by b_id;");
			int num = 0;
			int b_id = 0;
			System.out.println(id + "님의 대여 목록은 다음과 같습니다.");
			while (rs.next()) {
				b_id = rs.getInt("b_id");
				String bName = rs.getString("bName");
				num++;
				System.out.println(num + "\tBook ID: " + b_id + "  | 도서: " + bName);
			}

			b_id = 0;
			System.out.println("반납할 도서의 Book ID를 입력하세요.");
			int inID = sc.nextInt();
			rs = st.executeQuery("select r_id, b_id, bName from res where b_id = " + inID + " and u_id = '" + id + "';");

			while (rs.next()) {
				b_id = rs.getInt("b_id");
			}

			if (b_id == 0) {
				System.out.println("대여 목록에 없는 도서입니다.");
			} 
			else {
				rs = st.executeQuery("select r_id, b_id, bName from res where b_id = " + inID + " and u_id = '" + id + "';");
				if (rs.next()) {
					int r_id = rs.getInt("r_id");
					b_id = rs.getInt("b_id");
					String bName = rs.getString("bName");
					st.executeUpdate("update Book set isRes = false where b_id = " + b_id + ";");
					st.executeUpdate("delete from res where r_id = " + r_id + ";");
					System.out.println("반납이 완료되었습니다. 해당 도서의 별점을 등록하시겠습니까?(등록을 원한다면 Y를 입력하세요.)");
					sc.nextLine();
					String in1 = sc.nextLine();
					if (in1.equals("Y")) {
						while (true) {
							System.out.println("등록할 별점을 입력해주세요");
							float star_new = sc.nextFloat();
							if (star_new >= 0 && star_new <= 5) {
								st.executeUpdate("insert into star values(default, " + b_id + ", '" + bName + "', "
										+ star_new + ");");
								break;
							}
							else {
								System.out.println("1~5 사이의 수를 입력해주세요");
							}
						}
						System.out.println("별점이 등록되었습니다. 이용해주셔서 감사합니다.");
					} 
					else {
						st.executeUpdate("insert into star values(default, " + b_id + ", '" + bName + "', null);");
						System.out.println("이용해주셔서 감사합니다.");
					}

				}
			}

		}
	}
	
	public static void NewBookInquiry(Scanner sc, Statement st, ResultSet rs) throws SQLException {
		sc.nextLine();
		System.out.println("찾고 싶은 신규 도서 혹은 저자를 입력하세요.");
		String keyword = sc.nextLine();
		String query = "";
		query = "select distinct * from NBook where nbName like '%" + keyword + "%' or nwName like '%" + keyword
				+ "%';";
		rs = st.executeQuery(query);

		int num = 0;
		while (rs.next()) {
			int n_id = rs.getInt("n_id");
			String nbName = rs.getString("nbName");
			String nwName = rs.getString("nwName");
			int nbyear = rs.getInt("nbyear");
			int enter = rs.getInt("enter");
			num++;
			System.out.println(num + "\tNew Book ID: " + n_id + "  | 도서: " + nbName + "   | 저자: " + nwName
					+ "   | 발행년도: " + nbyear + "   | 입고일: " + enter);
		}

	}
	
	public static void NewReservation(String id, Scanner sc, Statement st, ResultSet rs) throws SQLException {
		System.out.println("1. 신규 도서 예약\t2. 내 예약 조회\t3. 예약 취소");
		int input = sc.nextInt();
		if (input == 1) {
			System.out.println("예약할 도서의 Book ID를 입력하세요.");
			int inNID = sc.nextInt();
			int n_id = 0;
			int count = 0;
			String query = "";
			query = "select n_id from NBook where n_id = " + inNID + ";";
			rs = st.executeQuery(query);
			while (rs.next()) {
				n_id = rs.getInt("n_id");
			}
			
			if (n_id == 0) {
				System.out.println("신규 목록에 없는 도서입니다.");
			}
			else {
				rs = st.executeQuery("select * from n_res where n_id = '"+ n_id +"' and u_id = '" + id + "';");
				int check = 0;
				while(rs.next()) {
					check = rs.getInt("nr_id");
				}
				if(check == 0) {
					rs = st.executeQuery("select count(*) from n_res where n_id = " + n_id + ";");
					while(rs.next()) {
						count = rs.getInt("count");
					}
					if(count > 5) {
						System.out.println("예약이 다 찼습니다.");
					}
					else {
						rs = st.executeQuery("select n_id, nbName, enter from NBook where n_id = " + inNID + ";");
						
						if (rs.next()) {
							n_id = rs.getInt("n_id");
							String nbName = rs.getString("nbName");
							int enter = rs.getInt("enter");
									
							st.executeUpdate("insert into n_res values(default, '" + id + "', " + n_id + ", '" + nbName + "', " + enter + ");");
						    System.out.println("예약이 완료되었습니다. 해당 도서는 " + enter + " 이후부터 대여 가능합니다.");
						}
					}
				}
				else {
					System.out.println("이미 예약되어있습니다.");
				}
			}
			
		}
		
		else if (input == 2) {
			int cnt = 0;
			rs = st.executeQuery("select count(*) from n_res where u_id = '" + id + "';");
			while (rs.next()) {
				cnt = rs.getInt("count");
			}
			if (cnt == 0) {
				System.out.println("예약한 도서가 없습니다.");
			}
			else {
				rs = st.executeQuery("select u_id, n_id, nbName, rentdate from n_res where u_id = '" + id + "' order by n_id;");
				int num = 0;
				int n_id = 0;
				System.out.println(id + "님의 예약 목록은 다음과 같습니다.");
				while (rs.next()) {
					n_id = rs.getInt("n_id");
					String nbName = rs.getString("nbName");
					int rentdate = rs.getInt("rentdate");
					num++;
					System.out.println(num + "\tNew Book ID: " + n_id + "  | 도서: " + nbName + "   | 대여가능 날짜: " + rentdate);
				}
			}
		}
		
		else if (input == 3) {
			int cnt = 0;
			rs = st.executeQuery("select count(*) from n_res where u_id = '" + id + "';");
			while (rs.next()) {
				cnt = rs.getInt("count");
			}
			if (cnt == 0) {
				System.out.println("예약한 도서가 없습니다.");
			}
			else {
				System.out.println("예약 취소를 원하는 New Book ID를 입력하세요.");
				int inNID = sc.nextInt();
				int n_id = 0;
				rs = st.executeQuery("select n_id from n_res where n_id = " + inNID + " and u_id = '" + id + "';");
				while(rs.next()) {
					n_id = rs.getInt("n_id");
				}
				if(n_id == 0) {
					System.out.println("예약한 도서가 아닙니다.");
				}
				else {
					rs = st.executeQuery("select nr_id from n_res where n_id = " + inNID + " and u_id = '" + id + "';");
					if(rs.next()) {
						int nr_id = rs.getInt("nr_id");
						st.executeUpdate("delete from n_res where nr_id = " + nr_id + ";");
						System.out.println("예약이 취소되었습니다.");
					}
				}
			}
		}
	}
	
	public static void SelectCustomBook(Scanner sc, Statement st, ResultSet rs) throws SQLException {
		System.out.println("원하는 기준을 선택하세요.");
		System.out.println("1. 평점 , 2. 대여횟수 , 3. 종합");
		int input = sc.nextInt();
		if (input == 1) {
			System.out.println("0~5 사이의 기준 평점을 입력하세요.");
			float score = sc.nextFloat();
			rs = st.executeQuery("select b_id, bName, avg(star) from star group by b_id, bName having avg(star) >= " + score + ";");

			int num = 0;
			System.out.println("평점이 " + score + "점 이상인 도서 목록은 다음과 같습니다.");
			while (rs.next()) {
				int b_id = rs.getInt("b_id");
				String bName = rs.getString("bName");
				float avg = rs.getFloat("avg");
				num++;
				System.out.println(num + "\tBook ID: " + b_id + "  | 도서: " + bName + "   | 평점: " + avg);
			}
			
		} else if (input == 2) {
			System.out.println("기준 대여횟수를 입력하세요.");
			int time = sc.nextInt();
			rs = st.executeQuery("select b_id, bName, count(*) from star group by b_id, bName having count(*) >= " + time + ";");

			int num = 0;
			System.out.println("대여횟수가 " + time + "번 이상인 도서 목록은 다음과 같습니다.");
			while (rs.next()) {
				int b_id = rs.getInt("b_id");
				String bName = rs.getString("bName");
				int count = rs.getInt("count");
				num++;
				System.out.println(num + "\tBook ID: " + b_id + "  | 도서: " + bName + "   | 대여횟수: " + count);
			}
			
		} else if (input == 3) {
			System.out.println("0~5 사이의 기준 평점을 입력하세요.");
			float score = sc.nextFloat();
			System.out.println("기준 대여횟수를 입력하세요.");
			int time = sc.nextInt();
			rs = st.executeQuery("select b_id, bName, avg(star), count(*) from star group by b_id, bName having avg(star) >= " + score
					+ " and count(*) >= " + time + ";");

			int num = 0;
			System.out.println("평점이 " + score + "점 이상이고, 대여횟수가 " + time + "번 이상인 도서 목록은 다음과 같습니다.");
			while (rs.next()) {
				int b_id = rs.getInt("b_id");
				String bName = rs.getString("bName");
				float avg = rs.getFloat("avg");
				int count = rs.getInt("count");
				num++;
				System.out.println(num + "\tBook ID: " + b_id + "  | 도서: " + bName + "   | 평점: " + avg + "   | 대여횟수: " + count);
			}
		}
	}
}
