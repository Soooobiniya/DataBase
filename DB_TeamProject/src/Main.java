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
            	
            	/* 5�� �̻�, ���� ����� ���� å ������ ���� ���ϵ��� TRIGGER ���� */
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
            	
            	
            	System.out.println("1. �α���, 2. ȸ������");
            	input = scan.nextInt();
            	
            	if(input == 1) {
            		id = login(scan, st, rs);
            		if(id.isEmpty()) {
            			return;
            		}
            	}
            	else if(input == 2) {
            		register(scan, st, rs);
            		System.out.println("�α����� �����ϰڽ��ϴ�.");
            		id = login(scan, st, rs);
            		if(id.isEmpty()) {
            			return;
            		}
            	}
            	else {
            		System.out.println("�ùٸ��� ���� �����Դϴ�.");
            		return;
            	}
            	
            	while(true) {
            		System.out.println("0. ���� , 1. ���� �˻� , 2. ���� �뿩 , 3. ���� �ݳ� , 4. �ű� ���� ��ȸ , 5. �ű� ���� ���� , 6. ���� ���� ����");
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
            			System.out.println("�ùٸ��� ���� �����Դϴ�.");
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
		System.out.println("���̵� �Է��ϼ���");
		String id = sc.nextLine();
		System.out.println("�н����带 �Է��ϼ���");
		String pw = sc.nextLine();
		String query = "";
		query = "select u_id from users where u_id = '" + id + "' and pw = '" + pw + "';";
		rs = st.executeQuery(query);
		if(rs.next()) {
			System.out.println("�α��� ����");
			return rs.getString("u_id");
		}
		System.out.println("�α��� ����");
		return "";
	}
	
	public static void register(Scanner sc, Statement st, ResultSet rs) throws SQLException {
		sc.nextLine();
		System.out.println("���̵� �Է��ϼ���");
		String id = sc.nextLine();
		String query = "";
		query = "select u_id from users where u_id = '" + id + "';";
		rs = st.executeQuery(query);
		if(rs.next()) {
			System.out.println("�ߺ��� ���̵� �ֽ��ϴ�");
			register(sc, st, rs);
		}
		else {
			System.out.println("�н����带 �Է��ϼ���");
			String pw = sc.nextLine();
			query = "insert into users values('" + id + "', '" + pw + "');";
			st.executeUpdate(query);
			System.out.println("ȸ�������� �Ϸ�Ǿ����ϴ�");
		}
		return;
	}
	
	public static void BookSearch(Scanner sc, Statement st, ResultSet rs) throws SQLException {
		sc.nextLine();
		System.out.println("ã�� ���� ���� Ȥ�� ���ڸ� �Է��ϼ���.");
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
					num + "\tBook ID: " + b_id + "  | ����: " + bName + "   | ����: " + wName + "   | ����⵵: " + byear);
		}
	}
	
	public static void BookLent(String id, Scanner sc, Statement st, ResultSet rs) throws SQLException {
		sc.nextLine();
		System.out.println("�뿩�� ������ Book ID�� �Է��ϼ���.");
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
				System.out.println("�뿩�� �Ϸ�Ǿ����ϴ�.");
			} else
				System.out.println("�̹� �뿩 ���� �����Դϴ�.");
		}
	}
	
	public static void BookReturn(String id, Scanner sc, Statement st, ResultSet rs) throws SQLException {
		int cnt = 0;
		rs = st.executeQuery("select count(*) from res where u_id = '" + id + "';");
		while (rs.next()) {
			cnt = rs.getInt("count");
		}
		if (cnt == 0) {
			System.out.println("�ݳ��� ������ �����ϴ�.");
		}
		else {
			rs = st.executeQuery("select u_id, b_id, bName from res where u_id = '" + id + "' order by b_id;");
			int num = 0;
			int b_id = 0;
			System.out.println(id + "���� �뿩 ����� ������ �����ϴ�.");
			while (rs.next()) {
				b_id = rs.getInt("b_id");
				String bName = rs.getString("bName");
				num++;
				System.out.println(num + "\tBook ID: " + b_id + "  | ����: " + bName);
			}

			b_id = 0;
			System.out.println("�ݳ��� ������ Book ID�� �Է��ϼ���.");
			int inID = sc.nextInt();
			rs = st.executeQuery("select r_id, b_id, bName from res where b_id = " + inID + " and u_id = '" + id + "';");

			while (rs.next()) {
				b_id = rs.getInt("b_id");
			}

			if (b_id == 0) {
				System.out.println("�뿩 ��Ͽ� ���� �����Դϴ�.");
			} 
			else {
				rs = st.executeQuery("select r_id, b_id, bName from res where b_id = " + inID + " and u_id = '" + id + "';");
				if (rs.next()) {
					int r_id = rs.getInt("r_id");
					b_id = rs.getInt("b_id");
					String bName = rs.getString("bName");
					st.executeUpdate("update Book set isRes = false where b_id = " + b_id + ";");
					st.executeUpdate("delete from res where r_id = " + r_id + ";");
					System.out.println("�ݳ��� �Ϸ�Ǿ����ϴ�. �ش� ������ ������ ����Ͻðڽ��ϱ�?(����� ���Ѵٸ� Y�� �Է��ϼ���.)");
					sc.nextLine();
					String in1 = sc.nextLine();
					if (in1.equals("Y")) {
						while (true) {
							System.out.println("����� ������ �Է����ּ���");
							float star_new = sc.nextFloat();
							if (star_new >= 0 && star_new <= 5) {
								st.executeUpdate("insert into star values(default, " + b_id + ", '" + bName + "', "
										+ star_new + ");");
								break;
							}
							else {
								System.out.println("1~5 ������ ���� �Է����ּ���");
							}
						}
						System.out.println("������ ��ϵǾ����ϴ�. �̿����ּż� �����մϴ�.");
					} 
					else {
						st.executeUpdate("insert into star values(default, " + b_id + ", '" + bName + "', null);");
						System.out.println("�̿����ּż� �����մϴ�.");
					}

				}
			}

		}
	}
	
	public static void NewBookInquiry(Scanner sc, Statement st, ResultSet rs) throws SQLException {
		sc.nextLine();
		System.out.println("ã�� ���� �ű� ���� Ȥ�� ���ڸ� �Է��ϼ���.");
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
			System.out.println(num + "\tNew Book ID: " + n_id + "  | ����: " + nbName + "   | ����: " + nwName
					+ "   | ����⵵: " + nbyear + "   | �԰���: " + enter);
		}

	}
	
	public static void NewReservation(String id, Scanner sc, Statement st, ResultSet rs) throws SQLException {
		System.out.println("1. �ű� ���� ����\t2. �� ���� ��ȸ\t3. ���� ���");
		int input = sc.nextInt();
		if (input == 1) {
			System.out.println("������ ������ Book ID�� �Է��ϼ���.");
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
				System.out.println("�ű� ��Ͽ� ���� �����Դϴ�.");
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
						System.out.println("������ �� á���ϴ�.");
					}
					else {
						rs = st.executeQuery("select n_id, nbName, enter from NBook where n_id = " + inNID + ";");
						
						if (rs.next()) {
							n_id = rs.getInt("n_id");
							String nbName = rs.getString("nbName");
							int enter = rs.getInt("enter");
									
							st.executeUpdate("insert into n_res values(default, '" + id + "', " + n_id + ", '" + nbName + "', " + enter + ");");
						    System.out.println("������ �Ϸ�Ǿ����ϴ�. �ش� ������ " + enter + " ���ĺ��� �뿩 �����մϴ�.");
						}
					}
				}
				else {
					System.out.println("�̹� ����Ǿ��ֽ��ϴ�.");
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
				System.out.println("������ ������ �����ϴ�.");
			}
			else {
				rs = st.executeQuery("select u_id, n_id, nbName, rentdate from n_res where u_id = '" + id + "' order by n_id;");
				int num = 0;
				int n_id = 0;
				System.out.println(id + "���� ���� ����� ������ �����ϴ�.");
				while (rs.next()) {
					n_id = rs.getInt("n_id");
					String nbName = rs.getString("nbName");
					int rentdate = rs.getInt("rentdate");
					num++;
					System.out.println(num + "\tNew Book ID: " + n_id + "  | ����: " + nbName + "   | �뿩���� ��¥: " + rentdate);
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
				System.out.println("������ ������ �����ϴ�.");
			}
			else {
				System.out.println("���� ��Ҹ� ���ϴ� New Book ID�� �Է��ϼ���.");
				int inNID = sc.nextInt();
				int n_id = 0;
				rs = st.executeQuery("select n_id from n_res where n_id = " + inNID + " and u_id = '" + id + "';");
				while(rs.next()) {
					n_id = rs.getInt("n_id");
				}
				if(n_id == 0) {
					System.out.println("������ ������ �ƴմϴ�.");
				}
				else {
					rs = st.executeQuery("select nr_id from n_res where n_id = " + inNID + " and u_id = '" + id + "';");
					if(rs.next()) {
						int nr_id = rs.getInt("nr_id");
						st.executeUpdate("delete from n_res where nr_id = " + nr_id + ";");
						System.out.println("������ ��ҵǾ����ϴ�.");
					}
				}
			}
		}
	}
	
	public static void SelectCustomBook(Scanner sc, Statement st, ResultSet rs) throws SQLException {
		System.out.println("���ϴ� ������ �����ϼ���.");
		System.out.println("1. ���� , 2. �뿩Ƚ�� , 3. ����");
		int input = sc.nextInt();
		if (input == 1) {
			System.out.println("0~5 ������ ���� ������ �Է��ϼ���.");
			float score = sc.nextFloat();
			rs = st.executeQuery("select b_id, bName, avg(star) from star group by b_id, bName having avg(star) >= " + score + ";");

			int num = 0;
			System.out.println("������ " + score + "�� �̻��� ���� ����� ������ �����ϴ�.");
			while (rs.next()) {
				int b_id = rs.getInt("b_id");
				String bName = rs.getString("bName");
				float avg = rs.getFloat("avg");
				num++;
				System.out.println(num + "\tBook ID: " + b_id + "  | ����: " + bName + "   | ����: " + avg);
			}
			
		} else if (input == 2) {
			System.out.println("���� �뿩Ƚ���� �Է��ϼ���.");
			int time = sc.nextInt();
			rs = st.executeQuery("select b_id, bName, count(*) from star group by b_id, bName having count(*) >= " + time + ";");

			int num = 0;
			System.out.println("�뿩Ƚ���� " + time + "�� �̻��� ���� ����� ������ �����ϴ�.");
			while (rs.next()) {
				int b_id = rs.getInt("b_id");
				String bName = rs.getString("bName");
				int count = rs.getInt("count");
				num++;
				System.out.println(num + "\tBook ID: " + b_id + "  | ����: " + bName + "   | �뿩Ƚ��: " + count);
			}
			
		} else if (input == 3) {
			System.out.println("0~5 ������ ���� ������ �Է��ϼ���.");
			float score = sc.nextFloat();
			System.out.println("���� �뿩Ƚ���� �Է��ϼ���.");
			int time = sc.nextInt();
			rs = st.executeQuery("select b_id, bName, avg(star), count(*) from star group by b_id, bName having avg(star) >= " + score
					+ " and count(*) >= " + time + ";");

			int num = 0;
			System.out.println("������ " + score + "�� �̻��̰�, �뿩Ƚ���� " + time + "�� �̻��� ���� ����� ������ �����ϴ�.");
			while (rs.next()) {
				int b_id = rs.getInt("b_id");
				String bName = rs.getString("bName");
				float avg = rs.getFloat("avg");
				int count = rs.getInt("count");
				num++;
				System.out.println(num + "\tBook ID: " + b_id + "  | ����: " + bName + "   | ����: " + avg + "   | �뿩Ƚ��: " + count);
			}
		}
	}
}
