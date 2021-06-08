package featurecat.lizzie.analysis;

import featurecat.lizzie.Lizzie;
import featurecat.lizzie.gui.EngineData;
import featurecat.lizzie.gui.EngineFailedMessage;
import featurecat.lizzie.gui.LizzieFrame;
import featurecat.lizzie.gui.Message;
import featurecat.lizzie.gui.RightClickMenu;
import featurecat.lizzie.gui.SocketCheckVersion;
import featurecat.lizzie.rules.Stone;
import featurecat.lizzie.util.Utils;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

//import featurecat.lizzie.rules.Board;
import featurecat.lizzie.rules.BoardData;
import featurecat.lizzie.rules.Movelist;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.jdesktop.swingx.util.OS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * An interface with leelaz go engine. Can be adapted for GTP, but is
 * specifically designed for GCP's Leela Zero. leelaz is modified to output
 * information as it ponders see www.github.com/gcp/leela-zero
 */
public class Leelaz {
	private final ResourceBundle resourceBundle = Lizzie.config.useLanguage==0? ResourceBundle.getBundle("l10n.DisplayStrings"):(Lizzie.config.useLanguage==1? ResourceBundle.getBundle("l10n.DisplayStrings", new Locale("zh", "CN")): ResourceBundle.getBundle("l10n.DisplayStrings", new Locale("en", "US")));
	//private static final long MINUTE = 60 * 1000; // number of milliseconds in a minute

	// private long maxAnalyzeTimeMillis; // , maxThinkingTimeMillis;
	  int cmdNumber;
	  int modifyNumber;
	private int currentCmdNum;
	// public int modifyCmdNum;
	//private boolean isResponse=false;
	private ArrayDeque<String> cmdQueue;

	public Process process;

	private BufferedReader inputStream;
	private BufferedOutputStream outputStream;
	private BufferedReader errorStream;
	
	private boolean hasUnReadLine=false;
	private String unReadLine="";

	// public Board board;
	private List<MoveData> bestMoves;
	private List<MoveData> bestMovesPrevious;
	//private List<MoveData> bestMovesTemp;
	//public boolean canGetGenmoveInfo = false;
	private boolean underPonder=false;
	public boolean canGetSummaryInfo = false;
	//public boolean canGetChatInfo = false;
	//public boolean canGetGenmoveInfoGen = false;
	//public boolean getGenmoveInfoPrevious= false;
	//private List<LeelazListener> listeners;

	private boolean isPondering;
	private long startPonderTime;

	// fixed_handicap
	public boolean isSettingHandicap = false;

	// genmove
	public boolean isThinking = false;
	public boolean isInputCommand = false;

	public boolean getRcentLine=false;
	public String recentRulesLine="";
	public int usingSpecificRules=-1;//1=中国规则2=中古规则3=日本规则4=TT规则5=其他规则
	public boolean preload = false;
	public boolean started = false;
	public boolean isLoaded = false;
	public boolean isCheckingVersion;
	public boolean isCheckingName;
	public String initialCommand;
	public boolean isCheckinPda =false;
	public  boolean isKataGoPda =false;
	public boolean isDymPda =false;
	public boolean isStaticPda =false;
	public boolean canRestoreDymPda=false;
	public double pda =0;
	private double pdaBeforeGame=0;
	public double pdaCap =0;
	public boolean startAutoAna=false;
	// for Multiple Engine
	public String oriEngineCommand="";
	public String engineCommand;
	private List<String> commands;
//	private String currentWeightFile = "";
//	private String currentWeight = "";
	//public boolean switching = false;
	private int currentEngineN = -1;
	private ScheduledExecutorService executor;
	private ScheduledExecutorService executorErr;
	ArrayList<Double> tempcount = new ArrayList<Double>();
	// dynamic komi and opponent komi as reported by dynamic-komi version of leelaz
//	private float dynamicKomi = Float.NaN;
//	private float dynamicOppKomi = Float.NaN;
	
	public int version = -1;
//	public ArrayList<Integer> heatcount = new ArrayList<Integer>();
	public String currentEnginename = "";
	public String oriEnginename = "";
	public boolean autoAnalysed = false;
//	private boolean isSaving = false;
	public boolean isResigning = false;
//	public boolean isClosingAutoAna = false;
	public boolean isColorEngine = false;
	public int stage = -1;
	public float komi = 7.5f;
	public float orikomi =7.5f;
	public int blackResignMoveCounts = 0;
	public int whiteResignMoveCounts = 0;
	public boolean resigned = false;
//	public boolean isManualB=false;
//	public boolean isManualW=false;
	public boolean doublePass = false;
	public boolean outOfMoveNum = false;
	public boolean played = false;
	public boolean isKatago = false;
	public boolean isKatagoCustom = false;
	public boolean noAnalyze=false;
	public boolean isSai = false;
	private boolean isLeela=false;
	public boolean isChanged = false;
	public double scoreMean = 0;
	public double scoreStdev = 0;
	private boolean isCommandLine = false;
	public int width = 19;
	public int height = 19;
	public int oriWidth = 19;
	public int oriHeight = 19;
	public boolean firstLoad = false;
	 Message msg;
	public boolean playNow=false;
	public boolean isZen=false;
	public boolean requireResponseBeforeSend=false;
	public boolean noLcb=false;
	//private boolean isInfoLine = false;
	//private boolean isNotifying = false;
	public boolean isSSH = false;
	//public boolean isScreen = false;
	public boolean isheatmap = false;
	public boolean iskataHeatmapShowOwner = false;
	public ArrayList<Integer> heatcount = new ArrayList<Integer>();
	
	public long pkMoveStartTime;
	public long pkMoveTime;
	public boolean isBackGroundThinking=false;
	//private int prepareNoGetGenmoveInfo = -1;
	//public long pkMoveTimeAll=0;
	public long pkMoveTimeGame=0;
	public boolean canSuicidal=false;
	public int genmoveNode=0;
	public int anaGameResignCount=0;
	public double heatwinrate=-1;
	public int symmetry=0;
	public double heatScore;
	private boolean heatCanGetPolicy;
	private boolean heatCanGetOwnership;
	
	private boolean canheatRedraw=false;
	public ArrayList<Double> heatPolicy = new ArrayList<Double>();
	public ArrayList<Double> heatOwnership = new ArrayList<Double>();
	public boolean isGamePaused =false;
	//public boolean isReadyForGenmoveGame=false;
	//private boolean isModifying=false;
	//private int ignoreCmdNumber=0;
	public boolean isTuning=false;
	public boolean isNormalEnd=false;
	public boolean canCheckAlive=true;
	public boolean isLeela0110 = false;
	  private List<MoveData> leela0110BestMoves;
	  private Timer leela0110PonderingTimer;
	  private BoardData leela0110PonderingBoardData;
	  private static final int LEELA0110_PONDERING_INTERVAL_MILLIS = 1000;
	  public boolean javaSSHClosed=false;
	  public boolean useJavaSSH=false;  
	  public String ip;	  
	  public String port;	  
	  public String userName;	  
	  public String password;	  
	  public boolean useKeyGen;	  
	  public String keyGenPath;	  
	  public SSHController javaSSH;
	  private boolean stopByLimit=false;
		public boolean stopByPlayouts=false;
	  public boolean outOfPlayoutsLimit=false;
	 private  EngineFailedMessage engineFailedMessage;
	 public List<String> commandLists = new ArrayList<String>();
	 private boolean startGetCommandList=false;
	 private boolean endGetCommandList=false;
//private int refreshNumber=0;
	// private boolean isEstimating=true;
	/**
	 * Initializes the leelaz process and starts reading output
	 *
	 * @throws IOException
	 */
	public Leelaz(String engineCommand) throws IOException, JSONException {
		// board = new Board();
		bestMoves = new ArrayList<>();
		bestMovesPrevious = new ArrayList<>();
		//bestMovesTemp = new ArrayList<>();
	//	listeners = new CopyOnWriteArrayList<>();

		isPondering = false;
		startPonderTime = System.currentTimeMillis();
		cmdNumber = 1;
		currentCmdNum = 0;
		cmdQueue = new ArrayDeque<>();
		setEngineCommand(engineCommand);
	}	
	
	public String getEngineCommand() {
		if(oriEngineCommand.startsWith("encryption||"))
			return resourceBundle.getString("Leelaz.encryption");
		return engineCommand;
	}
	  
	public void setEngineCommand(String commandString)
	{
		oriEngineCommand=commandString;
		if(commandString.startsWith("encryption||"))
		{	
			commandString=commandString.substring(12);			
			commandString=Utils.doDecrypt2(commandString);			
		}
		this.engineCommand = commandString==null?oriEngineCommand:commandString;
		if (this.engineCommand.toLowerCase().contains("override-version")) {
			this.isKatago = true;
		}
		if (this.engineCommand.toLowerCase().contains("katajigo")) {
			this.noAnalyze = true;
		}
		if (this.engineCommand.toLowerCase().contains("gogui")) {
			this.requireResponseBeforeSend = true;
		}
		if (this.engineCommand.toLowerCase().contains("ssh")||engineCommand.toLowerCase().contains("plink")) {
			this.isSSH = true;
		}
//		if (this.engineCommand.startsWith("screen")) {
//			this.engineCommand=this.engineCommand.substring(6);
//			this.isScreen = true;
//			}		
	}
//	public void updateCommand(String engineCommand) {
//		this.engineCommand = engineCommand;
//		if (engineCommand.toLowerCase().contains("override-version")) {
//			this.isKatago = true;
//		}
//		if (engineCommand.toLowerCase().contains("zen")) {
//			this.isZen = true;
//		}
//		if (engineCommand.toLowerCase().contains("ssh")) {
//			this.isSSH = true;
//		}
//	}
	
//	private String formateSaveString (String filename)
//	{
//		filename=filename.replaceAll("[/\\\\:*?|]", ".");
//		filename=filename.replaceAll("[\"<>]", "'");
//		return filename;
//	}

	public String getEngineName(int index) {
		if(index<0)
			return resourceBundle.getString("Menu.noEngine");
		 ArrayList<EngineData> engineData=Utils.getEngineData();		
		 currentEnginename =  engineData.get(index).name;
			oriEnginename = currentEnginename;		
		String   regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]"; 
		String aa = "";
		 Pattern p = Pattern.compile(regEx);
		  Matcher m = p.matcher(currentEnginename);
		  currentEnginename = m.replaceAll(aa).trim();		
//		if (currentEnginename.equals(""))
//			currentEnginename = currentWeight;
//		if (oriEnginename.equals(""))
//			oriEnginename = currentWeight;
		return currentEnginename;
	}

	public void startEngine(int index) throws IOException {
		if (engineCommand.trim().isEmpty()) {
			Utils.showMsg(resourceBundle.getString("EngineFaied.empty"));
			return;
		}	
		currentEngineN = index;
		canRestoreDymPda=false;
		commands = Utils.splitCommand(engineCommand);
		pda = 0;
		// Get weight name
	//	Pattern wPattern = Pattern.compile("(?s).*?(--weights |-w |-model )([^'\" ]+)(?s).*");
		//Matcher wMatcher = wPattern.matcher(engineCommand);		
		 currentEnginename = getEngineName(index);		
		
		 if (this.useJavaSSH) {
		      this.javaSSH = new SSHController(this, this.ip, this.port);
		      boolean loginStatus = false;
		      if (this.useKeyGen) {
		        loginStatus = this.javaSSH.loginByFileKey(this.engineCommand, this.userName, new File(this.keyGenPath)).booleanValue();
		      } else {
		        loginStatus = this.javaSSH.login(this.engineCommand, this.userName, this.password).booleanValue();
		      } 
		      if (loginStatus) {
		        this.javaSSHClosed = false;
		        this.inputStream = new BufferedReader(new InputStreamReader(this.javaSSH.getStdout()));
		        this.outputStream = new BufferedOutputStream(this.javaSSH.getStdin());
		        this.errorStream = new BufferedReader(new InputStreamReader(this.javaSSH.getSterr()));
		      } else {
		        return;
		      } 
		    } else {
		ProcessBuilder processBuilder = new ProcessBuilder(commands);
		processBuilder.redirectErrorStream(false);
		try {
			process = processBuilder.start();
		} catch (IOException e) {		
			      String err = e.getLocalizedMessage();
				try {					
						tryToDignostic(resourceBundle.getString("Leelaz.engineFailed")+": "+((err == null) ?resourceBundle.getString("Leelaz.engineStartNoExceptionMessage") : err),true);	
						LizzieFrame.openMoreEngineDialog();
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			return;
		}
		initializeStreams();
		}
		// Send a version request to check that we have a supported version
		// Response handled in parseLine
		isCheckingVersion = true;
		isCheckingName = true;
		endGetCommandList=false;
		// sendCommand("turnon");
		if(!isSSH)
		{									
		sendCommand("name");		
		sendCommand("version");		
		sendCommand("list_commands");	
		if(!(Lizzie.frame.isPlayingAgainstLeelaz||Lizzie.frame.isAnaPlayingAgainstLeelaz))
		sendCommand("komi " + komi);
		boardSize(width, height);
		if(initialCommand!=null&&!initialCommand.equals("")) {
			String[]  initialCommands= initialCommand.trim().split(";");
			for(String command:initialCommands)
			{sendCommand(command);}
		}
		}
		if(this==Lizzie.leelaz)
		Lizzie.board.getHistory().getGameInfo().setKomi(komi);
		if(isSSH) {
		 Runnable runnable =
			        new Runnable() {
			          public void run() {
			        	  try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
//			        	  if(isScreen)
//			  			{
//			        		  sendCommand("screen -r yzy1");	
//			        		  sendCommand("screen -r yzy1");
//			  			sendCommand("clear_board");	
//			  			}
			        	  sendCommand("name");
			      		sendCommand("name");
			      			sendCommand("name");			      			
			      			sendCommand("version");	
			      			sendCommand("list_commands");		      		
			      			boardSize(width, height);
			      			if(!(Lizzie.frame.isPlayingAgainstLeelaz||Lizzie.frame.isAnaPlayingAgainstLeelaz))
			      				sendCommand("komi " + komi);
			      			if(initialCommand!=null&&!initialCommand.equals("")) {
			      				String[]  initialCommands= initialCommand.trim().split(";");
			      				for(String command:initialCommands)
			      				{sendCommand(command);}
			      			}
			      			setResponseUpToDate();	
			          }
			        };
			    Thread thread = new Thread(runnable);
			    thread.start();
		}
		//if(width!=19||height!=19)
	

		// start a thread to continuously read Leelaz output
		// new Thread(this::read).start();
		// can stop engine for switching weights
		executor = Executors.newSingleThreadScheduledExecutor();
		isNormalEnd=false;	
		executor.execute(this::read);
		executorErr = Executors.newSingleThreadScheduledExecutor();
		executorErr.execute(this::readError);
		started = true;
	
		if(Lizzie.leelaz2!=null&&this==Lizzie.leelaz2)
		{
			if(index>19)
				Lizzie.frame.menu.changeEngineIcon2(20,1);
			else
				Lizzie.frame.menu.changeEngineIcon2(index,1);
		}
		else
		{
			if(index>19)
				Lizzie.frame.menu.changeEngineIcon(20,1);
			else
				Lizzie.frame.menu.changeEngineIcon(index,1);
		}
		  if(Lizzie.frame.isShowingHeatmap)
			  Lizzie.frame.isShowingHeatmap=false;
		  if(Lizzie.frame.isShowingPolicy)
			  Lizzie.frame.isShowingPolicy=false;
	}

//	public void restartEngine(int index) throws IOException {
//		if (engineCommand.trim().isEmpty()) {
//			return;
//		}
//		//switching = true;
//		this.engineCommand = engineCommand;
//		// stop the ponder
//		if (Lizzie.leelaz.isPondering()) {
//			Lizzie.leelaz.togglePonder();
//		}
//		normalQuit();
//		startEngine(index);
//		// currentEngineN = index;
//		togglePonder();
//	}

	public void restartClosedEngine(int index) throws IOException {
		boolean isPondering=this.isPondering;
		if (engineCommand.trim().isEmpty()) {
			return;
		}
		isLoaded=false;
		canCheckAlive=false;
		ArrayList<Movelist> mv = Lizzie.board.getmovelist();
		startEngine(index);
		Leelaz thisLeelz=this;
		 Runnable syncBoard =
		          new Runnable() {
		            public void run() {
		              while (!isLoaded() || isCheckingName) {
		                try {
		                  Thread.sleep(100);
		                } catch (InterruptedException e) {
		                  // TODO Auto-generated catch block
		                  e.printStackTrace();
		                }
		              }
		              if(isPondering)
		  Lizzie.board.restoreMoveNumber(index, mv, false,thisLeelz);
		              else
		            	  Lizzie.board.setmovelist(mv,false);
		            }
         };
     Thread syncBoardTh = new Thread(syncBoard);
     syncBoardTh.start();

	}

	public void normalQuit() {
		isNormalEnd=true;
		leela0110StopPonder();
		if(Lizzie.leelaz2!=null&&this==Lizzie.leelaz2)
		{
			if(currentEngineN>20)
				Lizzie.frame.menu.changeEngineIcon2(20,0);
			else
				Lizzie.frame.menu.changeEngineIcon2(currentEngineN,0);
		}
		else
		{
			if(currentEngineN>20)
				Lizzie.frame.menu.changeEngineIcon(20,0);
			else
				Lizzie.frame.menu.changeEngineIcon(currentEngineN,0);
		}

//		if(isScreen)
//			sendCommand("name");
		sendCommand("quit");
		if(this.useJavaSSH)
		{
			javaSSH.close();
		}
		else {
		executor.shutdown();
		try {
			while (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
			if (executor.awaitTermination(1, TimeUnit.SECONDS)) {
				shutdown();
			}
		} catch (InterruptedException e) {
			executor.shutdownNow();
			Thread.currentThread().interrupt();
		}
		}
		started = false;
		isLoaded=false;
	}
	
	public void forceQuit() {
		isNormalEnd=true;
		leela0110StopPonder();
//		if(isScreen)
//			sendCommand("name");
		if(Lizzie.leelaz2!=null&&this==Lizzie.leelaz2)
		{
			if(currentEngineN>20)
				Lizzie.frame.menu.changeEngineIcon2(20,0);
			else
				Lizzie.frame.menu.changeEngineIcon2(currentEngineN,0);
		}
		else
		{
			if(currentEngineN>20)
				Lizzie.frame.menu.changeEngineIcon(20,0);
			else
				Lizzie.frame.menu.changeEngineIcon(currentEngineN,0);
		}
		if(this.useJavaSSH)
		{
			javaSSH.close();
		}
		else {
		 try {
		       process.destroyForcibly();
		      } catch (Exception e) {
		      }}
		started = false;
		isLoaded=false;
		outputStream=null;
	}

	/** Initializes the input and output streams */
	public void initializeStreams() {
		inputStream = new BufferedReader(new  InputStreamReader(process.getInputStream()));
		outputStream = new BufferedOutputStream(process.getOutputStream());
		errorStream =new BufferedReader( new  InputStreamReader(process.getErrorStream()));
	}
	
	public List<MoveData> parseInfoSai(String line) {
		List<MoveData> bestMoves = new ArrayList<>();
		String[] variations = line.split(" info ");
		//int k = (Lizzie.config.limitMaxSuggestion > 0&&!Lizzie.config.showNoSuggCircle ? Lizzie.config.limitMaxSuggestion : 361);
		for (String var : variations) {
			if (!var.trim().isEmpty()) {
				bestMoves.add(MoveData.fromInfoSai(var));
			//	k = k - 1;
			//	if (k < 1)
			//		break;
			}
		}
		
		if(Lizzie.frame.extraMode==2&&Lizzie.leelaz2!=null&&this==Lizzie.leelaz2)			
			Lizzie.board.getData().tryToSetBestMoves2(bestMoves,currentEnginename,true);
		else {
			if(Lizzie.engineManager.isEngineGame&&Lizzie.config.enginePkPonder)
			{	if((Lizzie.board.getHistory().isBlacksTurn()&&this==Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex))||!Lizzie.board.getHistory().isBlacksTurn()&&this==Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex))
			{
			//	if(!isModifying)
				Lizzie.board.getData().tryToSetBestMoves(bestMoves,currentEnginename,true);
			}
			}
			else
				Lizzie.board.getData().tryToSetBestMoves(bestMoves,currentEnginename,true);
		}		
		return bestMoves;
	}
	
//	public List<MoveData> parseInfoSpe(String line) {
//		List<MoveData> bestMoves = new ArrayList<>();
//				bestMoves.add(MoveData.fromInfoSpec(line));
//		if(Lizzie.frame.extraMode==2&&Lizzie.leelaz2!=null&&this==Lizzie.leelaz2)			
//			Lizzie.board.getData().tryToSetBestMoves2(bestMoves,currentEnginename);
//		else
//		 {
//			if(Lizzie.engineManager.isEngineGame&&Lizzie.config.enginePkPonder)
//			{	if((Lizzie.board.getHistory().isBlacksTurn()&&this==Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex))||!Lizzie.board.getHistory().isBlacksTurn()&&this==Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex))
//			{
//				Lizzie.board.getData().tryToSetBestMoves(bestMoves,currentEnginename);
//			}
//			}
//			else
//				Lizzie.board.getData().tryToSetBestMoves(bestMoves,currentEnginename);
//		}
//		return bestMoves;
//	}

	public List<MoveData> parseInfo(String line) {
		List<MoveData> bestMoves = new ArrayList<>();
		String[] variations = line.split(" info ");
	//	int k = (Lizzie.config.limitMaxSuggestion > 0&&!Lizzie.config.showNoSuggCircle ? Lizzie.config.limitMaxSuggestion : 361);
		for (String var : variations) {
			if (!var.trim().isEmpty()) {
				bestMoves.add(MoveData.fromInfo(var));
			//	k = k - 1;
			//	if (k < 1)
			//		break;
			}
		}
		if(Lizzie.frame.extraMode==2&&Lizzie.leelaz2!=null&&this==Lizzie.leelaz2)			
			Lizzie.board.getData().tryToSetBestMoves2(bestMoves,currentEnginename,true);
		else
		 {
			if(Lizzie.engineManager.isEngineGame&&Lizzie.config.enginePkPonder)
			{	if((Lizzie.board.getHistory().isBlacksTurn()&&this==Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex))||!Lizzie.board.getHistory().isBlacksTurn()&&this==Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex))
			{
				//if(!isModifying)
				Lizzie.board.getData().tryToSetBestMoves(bestMoves,currentEnginename,true);
			}
			}
			else
				Lizzie.board.getData().tryToSetBestMoves(bestMoves,currentEnginename,true);
		}
		return bestMoves;
	}

	public List<MoveData> parseInfoKatago(String line) {
		List<MoveData> bestMoves = new ArrayList<>();
		String[] variations = line.split(" info ");
		//int k = (Lizzie.config.limitMaxSuggestion > 0&&!Lizzie.config.showNoSuggCircle ? Lizzie.config.limitMaxSuggestion : 361);
		for (String var : variations) {
			if (!var.trim().isEmpty()) {
				bestMoves.add(MoveData.fromInfoKatago(var));
		//		k = k - 1;
		//		if (k < 1)
		//			break;
			}
		}
		if(Lizzie.frame.extraMode==2&&Lizzie.leelaz2!=null&&this==Lizzie.leelaz2)			
			Lizzie.board.getData().tryToSetBestMoves2(bestMoves,currentEnginename,true);
		else
		 {
			if(Lizzie.engineManager.isEngineGame&&Lizzie.config.enginePkPonder)
			{	if((Lizzie.board.getHistory().isBlacksTurn()&&this==Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex))||!Lizzie.board.getHistory().isBlacksTurn()&&this==Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex))
			{
			//	if(!isModifying)
				Lizzie.board.getData().tryToSetBestMoves(bestMoves,currentEnginename,true);
			}
			}
			else
				Lizzie.board.getData().tryToSetBestMoves(bestMoves,currentEnginename,true);
		}
		return bestMoves;
	}



	/**
	 * Parse a line of Leelaz output
	 *
	 * @param line output line
	 * @throws IOException 
	 */

	private void parseLineForGenmovePk(String line) throws IOException {
		//Lizzie.gtpConsole.addLineforce(line);		
		
		if (line.startsWith("inf")) {
			if(this!=Lizzie.leelaz&&isResponseUpToDate()) {	
								if (isKatago) {
									this.bestMoves = parseInfoKatago(line.substring(5));
									if (Lizzie.config.showKataGoEstimate) {
										if (line.contains("ownership")) {
											tempcount = new ArrayList<Double>();
											String[] params = line.trim().split("ownership");
											String[] params2 = params[1].trim().split(" ");
											for (int i = 0; i < params2.length; i++)
												tempcount.add(Double.parseDouble(params2[i]));								
											Lizzie.frame.drawKataEstimate(this,tempcount);
										}
									}
								}
								else if(isSai) {
									this.bestMoves = parseInfoSai(line.substring(5));		
								} else {
									this.bestMoves = parseInfo(line.substring(5));						
								}
								Lizzie.frame.refresh(1);
								}			
			return;
						}
		else
			if(Lizzie.gtpConsole.isVisible()||Lizzie.config.alwaysGtp||!this.isLoaded)
					Lizzie.gtpConsole.addLine(line+"\n");
		if(isCheckinPda)
		{
			if(line.startsWith("pda:"))
			{
				isDymPda= true;				
				String[] params=line.trim().split(" ");
				if(params.length==2)
				pda=Double.parseDouble(params[1]);
				  Lizzie.frame.menu.txtPDA.setText(String.format("%.3f",pda));
				  if( Lizzie.frame.menu.setPda!=null)
				  Lizzie.frame.menu.setPda.curPDA.setText(String.format("%.3f",pda));
				if(Lizzie.config.chkAutoPDA)
				{
				sendCommand(Lizzie.config.AutoPDA);					
				if(Lizzie.config.chkDymPDA)
					{this.pdaCap=Double.parseDouble(Lizzie.config.dymPDACap.trim());
				if(Lizzie.frame.menu.setPda!=null)
					Lizzie.frame.menu.setPda.txtDymCap.setText(Lizzie.config.dymPDACap);}
				 if(Lizzie.config.chkStaticPDA)
					{Lizzie.frame.menu.txtPDA.setText(Lizzie.config.staticPDAcur);
					 this.pda=Double.parseDouble(Lizzie.config.staticPDAcur.trim());
					 isStaticPda=true;
					}
				 else
				 {
					 isStaticPda=false;
				 }
				}
			}
			if(line.startsWith("PDACap:"))
			{
				String[] params=line.trim().split(" ");
				if(params.length==2) {
				//	if(pdaCap==0)
					pdaCap=Double.parseDouble(params[1]);
				if(pdaCap!=0&&!isStaticPda)
				{
					isStaticPda=false;	
				    Runnable syncDymPda =
				            new Runnable() {
				              public void run() {
				            	  int i=0;
				                while (!canRestoreDymPda) {
				                  try {
				                	  i++;
				                	  if(i>19)
				                		  break;
				                    Thread.sleep(50);
				                  } catch (InterruptedException e) {
				                    // TODO Auto-generated catch block
				                    e.printStackTrace();
				                  }
				                } 
				                canRestoreDymPda=false;
				                if(Lizzie.config.chkAutoPDA)
								sendCommand(Lizzie.config.AutoPDA);
				                else
				                sendCommand("dympdacap "+pdaCap);
								if(isPondering())
									ponder();
				                }
			          };
			          Thread syncDymPdaTh = new Thread(syncDymPda);
			          syncDymPdaTh.start();									
				}
				else
				{
					isStaticPda=true;
				}
					  if( Lizzie.frame.menu.setPda!=null)
					  Lizzie.frame.menu.setPda.txtDymCap.setText(pdaCap+"");
				}
			}
		}
		
		if (line.startsWith("=")||line.startsWith("play")) 
		{
			isCommandLine=true;
			String[] params = line.trim().split(" ");
			//currentCmdNum = Integer.parseInt(params[0].substring(1).trim());
			if(params.length<=1)
				return;
			if ( Lizzie.engineManager.isEngineGame && params.length >= 2) {
				if (Lizzie.board.getHistory().isBlacksTurn()
						&& (this.currentEngineN == Lizzie.engineManager.engineGameInfo.whiteEngineIndex)) {
					return;
				}
				if (!Lizzie.board.getHistory().isBlacksTurn()
						&& (this.currentEngineN == Lizzie.engineManager.engineGameInfo.blackEngineIndex)) {
				return;
				}			
				 if(this.isZen) 
				 {	 Lizzie.board.getData().tryToSetBestMoves(bestMoves, this.currentEnginename,true);
				 bestMoves = new ArrayList<>();}
				if (params[1].contains("resign")) {
					genmoveNode++;
					pkMoveTime=System.currentTimeMillis()-pkMoveStartTime;
					pkMoveTimeGame=pkMoveTimeGame+pkMoveTime;
					
					nameCmdfornoponder();
					genmoveResign(false);
					return;
				}
				if (Lizzie.frame.toolbar.checkGameMaxMove&&Lizzie.board.getHistory().getMoveNumber() > Lizzie.frame.toolbar.maxGanmeMove) {
					genmoveNode++;
					pkMoveTime=System.currentTimeMillis()-pkMoveStartTime;
					pkMoveTimeGame=pkMoveTimeGame+pkMoveTime;					
					   outOfMoveNum=true;	
						nameCmdfornoponder();
						genmoveResign(false);
					  return;
				   }
				boolean isPassingLose=false;
				if(params[1].startsWith("Passing")) {
					isPassingLose=true;
				}
				if (!isPassingLose&&params[1].startsWith("pass")) {		
					genmoveNode++;
					pkMoveTime=System.currentTimeMillis()-pkMoveStartTime;
					pkMoveTimeGame=pkMoveTimeGame+pkMoveTime;
					Optional<int[]> passStep = Optional.empty();
					Optional<int[]> lastMove = Lizzie.board.getLastMove();
					
					if (lastMove == passStep) {						
						doublePass = true;
						nameCmdfornoponder();
						genmoveResign(true);
						return;
					}					
					Lizzie.board.pass();
					if (this.currentEngineN == Lizzie.engineManager.engineGameInfo.blackEngineIndex) {					
						if(!Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex).playMoveGenmove("B",
								"pass"))
						{return;}
						Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex).genmoveForPk("W");
						if(!Lizzie.config.enginePkPonder)
						Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex).nameCmdfornoponder();
						Lizzie.leelaz = Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex);
					}

					else {						
						if(!Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex).playMoveGenmove("W",
								"pass"))
							{return;}
						Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex).genmoveForPk("B");
						if(!Lizzie.config.enginePkPonder)
						Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex).nameCmdfornoponder();
						Lizzie.leelaz = Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex);
					}					
					return;
				} else {
				//	try {					
					Optional<int[]> coords;
					if(isPassingLose) {						
							coords = Lizzie.board.asCoordinates(inputStream.readLine());							
					}
					else
						coords = Lizzie.board.asCoordinates(params[1]);					
					 if(!coords.isPresent())
					 {								 
						return;}
					 canCheckAlive=true;
						genmoveNode++;
						pkMoveTime=System.currentTimeMillis()-pkMoveStartTime;
						pkMoveTimeGame=pkMoveTimeGame+pkMoveTime;
						
					 Lizzie.board.place(coords.get()[0], coords.get()[1]);					 						
						
//					}
//					catch (Exception e)
//					{
//						return;
//					}				
					String coordsString=Lizzie.board.convertCoordinatesToName(coords.get()[0], coords.get()[1]);
					if (this.currentEngineN == Lizzie.engineManager.engineGameInfo.blackEngineIndex) {

						if(!Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex).playMoveGenmove("B",
								coordsString))
							{return;}
						Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex).genmoveForPk("W");
						if(!Lizzie.config.enginePkPonder)
						Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex).nameCmdfornoponder();
						Lizzie.leelaz = Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex);

					}

					else {
						if(!Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex).playMoveGenmove("W",
								coordsString))
							{return;}
						Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex).genmoveForPk("B");
						if(!Lizzie.config.enginePkPonder)
						Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex).nameCmdfornoponder();
						Lizzie.leelaz = Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex);
					}					
					return;
			}	
			}
			if (isCheckingName) {				
				pkMoveStartTime=System.currentTimeMillis();				
				isCheckingName = false;	
				//isReadyForGenmoveGame =true;
				isKataGoPda=false;
				if (params[1].toLowerCase().startsWith("zen"))
					this.isZen=true;
				if (params[1].toLowerCase().startsWith("llzero"))
				{	this.noLcb=true;
				this.isLeela=true;
				}
				if (params[1].toLowerCase().startsWith("leela") && params.length > 2&&params[2].toLowerCase().startsWith("zero"))				
				{	
				this.isLeela=true;
				}
				if (params[1].equals("Leela") && params.length == 2) {
		            isLeela0110 = true;
		            isLoaded=true;
		          }
				if (params[1].toLowerCase().startsWith("sai"))
					this.isSai=true;					
//				if (params[1].startsWith("KataGoYm"))
//					sendCommandToLeelazWithOutLog("lizzie_use");					
				if (params[1].startsWith("KataGo") || isKatago) {					
					if(params[1].startsWith("KataGoPda")) {
						isKatagoCustom=true;
						isCheckinPda=true;
						isKataGoPda=true;
					sendCommand("getpda");
					sendCommand("getdympdacap");
					Runnable runnable =
					        new Runnable() {
					          public void run() {
					        	  try {
									Thread.sleep(5000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
					        	  isCheckinPda=false;		      		
					          }
					        };
					    Thread thread = new Thread(runnable);
					    thread.start();
					}
					if(Lizzie.config.autoLoadKataRules)
						sendCommand("kata-set-rules "+Lizzie.config.kataRules);
					getSuicidalScadule();
					sendCommand("kata-get-rules");
					this.isKatago = true;
					if(params[1].startsWith("KataGoCustom")) 
						isKatagoCustom=true;
					this.version = 17;							
					isCheckingVersion = false;

					if (this.currentEngineN == EngineManager.currentEngineNo) {
						Lizzie.config.leelaversion = version;
					}
				//	isLoaded = true;
					setKataEnginePara();
					//Lizzie.frame.menu.showWRNandPDA(true);
				}	
				else {setLeelaSaiEnginePara();
				//Lizzie.frame.menu.showWRNandPDA(false);
				}
			} else if (isCheckingVersion && !isKatago&&!isLeela0110) {				
				String[] ver = params[1].split("\\.");
				try {
				int minor = Integer.parseInt(ver[1]);
				// Gtp support added in version 15
				version = minor;}
				catch(Exception ex)
				{
					version=17;
				}
				if (this.currentEngineN == EngineManager.currentEngineNo) {
					Lizzie.config.leelaversion = version;
				}
				if (version ==7) {
					version=17;
				}
				isCheckingVersion = false;						
			//	isLoaded = true;					
			}
		}
		else if(line.startsWith("?")) {
			isCommandLine=true;
		}
			
		
		if(Lizzie.gtpConsole.isVisible()||Lizzie.config.alwaysGtp)
			Lizzie.gtpConsole.addLine(line+"\n");

		else if(line.startsWith("PDA:"))
		{
			
			String[] params=line.trim().split(" ");
			if(params.length==2)
			pda=Double.parseDouble(params[1]);
			  Lizzie.frame.menu.txtPDA.setText(String.format("%.3f",pda));
			  if( Lizzie.frame.menu.setPda!=null)
			  Lizzie.frame.menu.setPda.curPDA.setText(String.format("%.3f",pda));
		}
		
	}

	private void parseLine(String line) {
		//System.out.println(line);
		synchronized (this) {			
			if (!played) 
			{
				if (line.startsWith("inf")) {
					if((isResponseUpToDate())) {
									if (Lizzie.engineManager.isEngineGame) {
										//Lizzie.frame.subBoardRenderer.reverseBestmoves = false;
										//Lizzie.frame.boardRenderer.reverseBestmoves = false;
										if(Lizzie.config.enginePkPonder)
										{	if((Lizzie.board.getHistory().isBlacksTurn()&&this==Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex))||!Lizzie.board.getHistory().isBlacksTurn()&&this==Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex))
										{
											Lizzie.leelaz = this;
										}
										}
										else
											Lizzie.leelaz = this;
										
									}
									// Clear switching prompt
									//switching = false;

									// Display engine command in the title
									Lizzie.frame.updateTitle();
									
										// This should not be stale data when the command number match	
										if (isKatago) {
											this.bestMoves = parseInfoKatago(line.substring(5));
											if (Lizzie.config.showKataGoEstimate) {
												if (line.contains("ownership")) {
													tempcount = new ArrayList<Double>();
													String[] params = line.trim().split("ownership");
													String[] params2 = params[1].trim().split(" ");
													for (int i = 0; i < params2.length; i++)
														tempcount.add(Double.parseDouble(params2[i]));								
													Lizzie.frame.drawKataEstimate(this,tempcount);
												}
											}
										}
										else if(isSai) {
											this.bestMoves = parseInfoSai(line.substring(5));		
										} else {
											this.bestMoves = parseInfo(line.substring(5));						
										}	
										if (!this.bestMoves.isEmpty()) {			
											if(Lizzie.config.enginePkPonder)
											{	
												//if((Lizzie.board.getHistory().isBlacksTurn()&&this==Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex))||!Lizzie.board.getHistory().isBlacksTurn()&&this==Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex))
											if(!isBackGroundThinking)
												{
												//if(isResponseUpToDate())							
													notifyAutoPK(false);
													pkResign();							
											}
											}
											else {
												notifyAutoPK(false);
												pkResign();
											}
								        	  notifyAutoPlay(false);		
								        	  if(Lizzie.config.isAutoAna)
								        	  {
								        		  if(Lizzie.frame.isAutoAnalyzingDiffNode) {
								        			  nofityDiffAna();
								        		  }
								        		  else
								        			  if(Lizzie.config.analyzeAllBranch)
								        		  {
								        			  notifyAutoAnaAllBranch();
								        		  }
								        		  else
								        		  {
								        			  notifyAutoAna();
								        		  }
								        	  }			        	 
										}
										if(!played)
										Lizzie.frame.refresh(1);
										// don't follow the maxAnalyzeTime rule if we are in analysis mode
										if ((!Lizzie.engineManager.isEngineGame&&!Lizzie.config.isAutoAna))
												{											
											 if(!outOfPlayoutsLimit&&Lizzie.config.limitPlayout&&getTotalPlayouts(bestMoves)>Lizzie.config.limitPlayouts) {
												stopByLimit=true;
												stopByPlayouts=true;
												isPondering = !isPondering;
												nameCmd();
											}
											 else if(Lizzie.config.limitTime&& (System.currentTimeMillis() - startPonderTime) > Lizzie.config.maxAnalyzeTimeMillis)
												{
													stopByLimit=true;
													isPondering = !isPondering;
													nameCmd();
												}
												}
									this.canCheckAlive=true;
									}	
									else
									{
										if(Lizzie.config.isAutoAna)
										bestMoves = new ArrayList<>();
									if(Lizzie.config.isAutoAna)
									Lizzie.board.getHistory().getCurrentHistoryNode().getData().tryToClearBestMoves();}								
					return;
								}
				else {
					 if(Lizzie.gtpConsole.isVisible()||Lizzie.config.alwaysGtp||!this.isLoaded)						
							Lizzie.gtpConsole.addLine(line+"\n");
				}
//			if (Lizzie.engineManager.isEngineGame && this.isPondering) {
//				Lizzie.engineManager.startInfoTime = System.currentTimeMillis();
//			}			
				if(isCheckinPda)
				{
					if(line.startsWith("pda:"))
					{
						isDymPda= true;
						
						String[] params=line.trim().split(" ");
						if(params.length==2)
						pda=Double.parseDouble(params[1]);
						  Lizzie.frame.menu.txtPDA.setText(String.format("%.3f",pda));
						  if( Lizzie.frame.menu.setPda!=null)
						  Lizzie.frame.menu.setPda.curPDA.setText(String.format("%.3f",pda));
						if(Lizzie.config.chkAutoPDA)
						{
						sendCommand(Lizzie.config.AutoPDA);						
						if(Lizzie.config.chkDymPDA)
							{this.pdaCap=Double.parseDouble(Lizzie.config.dymPDACap.trim());
						if(Lizzie.frame.menu.setPda!=null)
							Lizzie.frame.menu.setPda.txtDymCap.setText(Lizzie.config.dymPDACap);}
						 if(Lizzie.config.chkStaticPDA)
							{Lizzie.frame.menu.txtPDA.setText(Lizzie.config.staticPDAcur);
							 isStaticPda=true;
							 this.pda=Double.parseDouble(Lizzie.config.staticPDAcur.trim());;
							}
						 else
						 {
							 isStaticPda=false;
						 }
						}
						if(!Lizzie.engineManager.isEngineGame&&this==Lizzie.leelaz)
							ponder();
					}
					if(line.startsWith("PDACap:"))
					{
						String[] params=line.trim().split(" ");
						if(params.length==2) {
							//if(pdaCap==0)
							pdaCap=Double.parseDouble(params[1]);
						if(pdaCap!=0&&!isStaticPda)
						{
							isStaticPda=false;	
							 Runnable syncDymPda =
							            new Runnable() {
							              public void run() {
							            	  int i=0;
							                while (!canRestoreDymPda) {
							                  try {
							                	  i++;
							                	  if(i>19)
							                		  break;
							                    Thread.sleep(50);
							                  } catch (InterruptedException e) {
							                    // TODO Auto-generated catch block
							                    e.printStackTrace();
							                  }
							                } 
							                canRestoreDymPda=false;
							                if(Lizzie.config.chkAutoPDA)
												sendCommand(Lizzie.config.AutoPDA);
								                else
							                sendCommand("dympdacap "+pdaCap);
											if(isPondering()||Lizzie.frame.extraMode==2)
												ponder();
							                }
						          };
						          Thread syncDymPdaTh = new Thread(syncDymPda);
						          syncDymPdaTh.start();		
						}
						else
						{
							isStaticPda=true;							
						}
							  if( Lizzie.frame.menu.setPda!=null)
							  Lizzie.frame.menu.setPda.txtDymCap.setText(pdaCap+"");
						}
					}
				}
				if(this.isKatago){					
					if(line.startsWith("PDA:"))
					{
						
						//String[] params = line.trim().split(",");
						String[] params=line.trim().split(" ");
						if(params.length==2)
						pda=Double.parseDouble(params[1]);
						  Lizzie.frame.menu.txtPDA.setText(String.format("%.3f",pda));
						  if( Lizzie.frame.menu.setPda!=null)
						  Lizzie.frame.menu.setPda.curPDA.setText(String.format("%.3f",pda));
					}	
				}
				else {	if (line.startsWith("| ST")) {
						String[] params = line.trim().split(" ");
						if (params.length == 13) {
							isColorEngine = true;
							if(Lizzie.gtpConsole.isVisible()||Lizzie.config.alwaysGtp)
								Lizzie.gtpConsole.addLine(oriEnginename + ": " + line);
							stage = Integer.parseInt(params[3].substring(0, params[3].length() - 1));
							komi = Float.parseFloat(params[6].substring(0, params[6].length() - 1));	
					        
						}
					} 
				}				
		//if (!this.isScreen&&line.startsWith("play")) {
				if (line.startsWith("play")) {
			// In lz-genmove_analyze
			String[] params = line.trim().split(" ");
			if (isInputCommand) {
				//	getGenmoveInfoPrevious = true;
					Lizzie.board.place(params[1]);
					if(isPondering)
						ponder();
					else {
							nameCmdfornoponder();
					}
					if (Lizzie.frame.isAutocounting) {
						if (Lizzie.board.getHistory().isBlacksTurn())
							Lizzie.frame.zen.sendCommand("play " + "w " + params[1]);
						else
							Lizzie.frame.zen.sendCommand("play " + "b " + params[1]);

						Lizzie.frame.zen.countStones();
					}					
				}
				if (Lizzie.frame.isPlayingAgainstLeelaz) {
					if(params.length>1) {
					if (params[1].startsWith("resign")) {
						if (Lizzie.frame.playerIsBlack) {

							if(msg==null||!msg.isVisible())
			            	{	
							  msg=new Message();
				             msg.setMessage( resourceBundle.getString("Leelaz.blackWinAiResign"));
				        //     msg.setVisible(true);
			            	}
							GameInfo gameInfo = Lizzie.board.getHistory().getGameInfo();
							gameInfo.setResult(resourceBundle.getString("Leelaz.blackWin"));
							Lizzie.frame.setResult(resourceBundle.getString("Leelaz.blackWin"));

						} else {
							if(msg==null||!msg.isVisible())
			            	{	
							  msg=new Message();
				             msg.setMessage(resourceBundle.getString("Leelaz.whiteWinAiResign"));
				        //     msg.setVisible(true);
			            	}
							GameInfo gameInfo = Lizzie.board.getHistory().getGameInfo();
							gameInfo.setResult(resourceBundle.getString("Leelaz.whiteWin"));
							Lizzie.frame.setResult(resourceBundle.getString("Leelaz.whiteWin"));

						}
						togglePonder();
						return;
					}

					if (params[1].startsWith("pass")) {
						//getGenmoveInfoPrevious = true;
						Lizzie.board.pass();
						Lizzie.frame.menu.toggleEngineMenuStatus(false,false);
					} else {
						//getGenmoveInfoPrevious = true;
						Lizzie.board.place(params[1]);
						Lizzie.frame.menu.toggleEngineMenuStatus(false,false);
					}}
					if (Lizzie.frame.isAutocounting) {
						if (Lizzie.board.getHistory().isBlacksTurn())
							Lizzie.frame.zen.sendCommand("play " + "w " + params[1]);
						else
							Lizzie.frame.zen.sendCommand("play " + "b " + params[1]);

						Lizzie.frame.zen.countStones();
					}
					if (!Lizzie.config.playponder)
						Lizzie.leelaz.nameCmdfornoponder();
				}
				if (!isInputCommand) {
					isPondering = false;
				}
				isThinking = false;
				if (isInputCommand) {
					isInputCommand = false;
				}
			} else if (line.startsWith("=")) {	
				isCommandLine=true;
				if(startGetCommandList)
				{startGetCommandList=false;
				endGetCommandList=true;}
				String[] params = line.trim().split(" ");
				if (params.length == 1)
					return;			
				if (!endGetCommandList&&params.length==2&&params[1].equals("protocol_version"))
				{
					startGetCommandList=true;
				}
				if (isInputCommand) {
					//	getGenmoveInfoPrevious = true;
						Lizzie.board.place(params[1]);
						if(isPondering)
							ponder();
						else
							this.nameCmdfornoponder();
						if (Lizzie.frame.isAutocounting) {
							if (Lizzie.board.getHistory().isBlacksTurn())
								Lizzie.frame.zen.sendCommand("play " + "w " + params[1]);
							else
								Lizzie.frame.zen.sendCommand("play " + "b " + params[1]);

							Lizzie.frame.zen.countStones();
						}
						isInputCommand = false;
						isThinking = false;							
					}
				if (isSettingHandicap) {					
					bestMoves = new ArrayList<>();
					Lizzie.board.hasStartStone = true;
					for (int i = 1; i < params.length; i++) {
						Optional<int[]> coordsOpt=Lizzie.board.asCoordinates(params[i]);
						if(coordsOpt.isPresent()) {
							int[] coords=coordsOpt.get();
							Lizzie.board.getHistory().setStone(coords, Stone.BLACK);
							Lizzie.board.getHistory().getData().blackToPlay=false;
							Lizzie.board.setStartListStone(coords,true);
						}
					}					
					isSettingHandicap = false;
					if(Lizzie.frame.isAnaPlayingAgainstLeelaz)
					{
						  
						 if (Lizzie.config.UsePureNetInGame && !Lizzie.leelaz.isheatmap)
						     Lizzie.leelaz.toggleHeatmap(false);
						 Lizzie.leelaz.Pondering();                  
			                if(Lizzie.config.playponder||(Lizzie.board.getHistory().isBlacksTurn()&&!Lizzie.frame.playerIsBlack)||(!Lizzie.board.getHistory().isBlacksTurn()&&Lizzie.frame.playerIsBlack))
			                { 
			                Lizzie.leelaz.ponder();
			              }
					}				
					Lizzie.frame.refresh();						
				} else if (isThinking && !isPondering) {
					if (isInputCommand) {
						Lizzie.board.place(params[1]);
						togglePonder();
						if (Lizzie.frame.isAutocounting) {
							if (Lizzie.board.getHistory().isBlacksTurn())
								Lizzie.frame.zen.sendCommand("play " + "w " + params[1]);
							else
								Lizzie.frame.zen.sendCommand("play " + "b " + params[1]);

							Lizzie.frame.zen.countStones();
						}
					}
					if (Lizzie.frame.isPlayingAgainstLeelaz) {
						if (params[1].startsWith("resign")) {
							if (Lizzie.frame.playerIsBlack) {

								if(msg==null||!msg.isVisible())
				            	{	
								  msg=new Message();
					             msg.setMessage( resourceBundle.getString("Leelaz.blackWinAiResign"));
					          }
								GameInfo gameInfo = Lizzie.board.getHistory().getGameInfo();
								gameInfo.setResult(resourceBundle.getString("Leelaz.blackWin"));
								Lizzie.frame.setResult(resourceBundle.getString("Leelaz.blackWin"));

							} else {
								if(msg==null||!msg.isVisible())
				            	{	
								  msg=new Message();
					             msg.setMessage(resourceBundle.getString("Leelaz.whiteWinAiResign"));
					          }
								GameInfo gameInfo = Lizzie.board.getHistory().getGameInfo();
								gameInfo.setResult(resourceBundle.getString("Leelaz.whiteWin"));
								Lizzie.frame.setResult(resourceBundle.getString("Leelaz.whiteWin"));

							}
							togglePonder();
							return;
						}

						if (params[1].startsWith("pass")) {
							Lizzie.board.pass();
							Lizzie.frame.menu.toggleEngineMenuStatus(false,false);
						} else {
							Lizzie.board.place(params[1]);
							Lizzie.frame.menu.toggleEngineMenuStatus(false,false);
						}
						if (Lizzie.frame.isAutocounting) {
							if (Lizzie.board.getHistory().isBlacksTurn())
								Lizzie.frame.zen.sendCommand("play " + "w " + params[1]);
							else
								Lizzie.frame.zen.sendCommand("play " + "b " + params[1]);

							Lizzie.frame.zen.countStones();
						}
						if (!Lizzie.config.playponder)
							Lizzie.leelaz.nameCmdfornoponder();
					}
					if (!isInputCommand) {
						isPondering = false;
					}
					isThinking = false;
					if (isInputCommand) {
						isInputCommand = false;
					}
				} 
				
					if (isCheckingName) {
						noAnalyze=false;
						isCheckingName = false;						
						isKataGoPda=false;
						pkMoveStartTime=System.currentTimeMillis();
						if (params[1].toLowerCase().startsWith("golaxy"))
							requireResponseBeforeSend=true;
						else 
							requireResponseBeforeSend=false;
						if (params[1].toLowerCase().startsWith("zen"))
							this.isZen=true;
						if (params[1].toLowerCase().startsWith("llzero"))
							this.noLcb=true;
						if (params[1].toLowerCase().startsWith("sai"))
							this.isSai=true;
						if (params[1].toLowerCase().startsWith("leela")&&params.length>2&&params[2].toLowerCase().startsWith("zero"))				
						{	
							this.isLeela=true;
							}
						 if (params[1].equals("Leela") && params.length == 2) {
				            isLeela0110 = true;
				            isLoaded = true;
				          }
//						if (params[1].startsWith("KataGoYm"))
//							sendCommandToLeelazWithOutLog("lizzie_use");		
						if (params[1].startsWith("KataGo") || isKatago) {
							if(params[1].startsWith("KataGoPda")) {
								isKatagoCustom=true;
								isCheckinPda=true;								
								isKataGoPda=true;
							sendCommand("getpda");
							sendCommand("getdympdacap");
							Runnable runnable =
							        new Runnable() {
							          public void run() {
							        	  try {
											Thread.sleep(5000);
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
							        	  isCheckinPda=false;		      		
							          }
							        };
							    Thread thread = new Thread(runnable);
							    thread.start();
							}							
							if(Lizzie.config.autoLoadKataRules)
								sendCommand("kata-set-rules "+Lizzie.config.kataRules);
							getSuicidalScadule();
							sendCommand("kata-get-rules");
							this.isKatago = true;
							if(params[1].startsWith("KataGoCustom")) 
								isKatagoCustom=true;
							this.version = 17;							
							isCheckingVersion = false;

							if (this.currentEngineN == EngineManager.currentEngineNo) {
								Lizzie.config.leelaversion = version;
							}
							isLoaded = true;
							isTuning=false;							
							if(Lizzie.leelaz2!=null&&this==Lizzie.leelaz2)
							{
								if(currentEngineN>20)
									Lizzie.frame.menu.changeEngineIcon2(20,2);
								else
									Lizzie.frame.menu.changeEngineIcon2(currentEngineN,2);
							}
							else
							{
								if(currentEngineN>20)
									Lizzie.frame.menu.changeEngineIcon(20,2);
								else
									Lizzie.frame.menu.changeEngineIcon(currentEngineN,2);
							}
							setKataEnginePara();
						}	else {setLeelaSaiEnginePara();
						}		
						if (params[1].toLowerCase().startsWith("katajigo"))
						{this.isKatago=true;
					this.noAnalyze=true;}
					} else if (isCheckingVersion && !isKatago&&!isLeela0110) {
						String[] ver = params[1].split("\\.");
						try {
						int minor = Integer.parseInt(ver[1]);
						// Gtp support added in version 15
						version = minor;}
						catch(Exception ex)
						{
							version=17;
						}
						if (this.currentEngineN == EngineManager.currentEngineNo) {
							Lizzie.config.leelaversion = version;
						}
						if (version ==7) {
							version=17;
						}
						isCheckingVersion = false;						
						isLoaded = true;
						isTuning=false;
						//Lizzie.initializeAfterVersionCheck();
						if(Lizzie.leelaz2!=null&&this==Lizzie.leelaz2)
						{
							if(currentEngineN>20)
								Lizzie.frame.menu.changeEngineIcon2(20,2);
							else
								Lizzie.frame.menu.changeEngineIcon2(currentEngineN,2);
							
						}
						else
						{
							if(currentEngineN>20)
								Lizzie.frame.menu.changeEngineIcon(20,2);
							else
								Lizzie.frame.menu.changeEngineIcon(currentEngineN,2);						
						}
				}
			}
			else if (line.startsWith("?"))
			{
				isCommandLine=true;
			}
				parseHeatMap(line);
		}else {
			if(Lizzie.gtpConsole.isVisible()||Lizzie.config.alwaysGtp||!this.isLoaded)			
				if(!line.startsWith("inf"))
				Lizzie.gtpConsole.addLine(line+"\n");
			  if (line.startsWith("=") || line.startsWith("?")) {
				isCommandLine=true;}
		}
		}
	}

	private void notifyAutoPlay(boolean playImmediately) {
		if(this!=Lizzie.leelaz)
			return;
		if (Lizzie.frame.toolbar.isAutoPlay) {
			if ((Lizzie.board.getHistory().isBlacksTurn() && Lizzie.frame.toolbar.chkAutoPlayBlack.isSelected())
					|| (!Lizzie.board.getHistory().isBlacksTurn()
							&& Lizzie.frame.toolbar.chkAutoPlayWhite.isSelected())) {
				int time = 0;
				int playouts = 0;
				int firstPlayouts = 0;
				if (Lizzie.frame.toolbar.chkAutoPlayTime.isSelected()) {
					try {
						time = 1000 * Integer.parseInt(Lizzie.frame.toolbar.txtAutoPlayTime.getText().replace(" ", ""));
					} catch (NumberFormatException err) {
					}
				}
				if (Lizzie.frame.toolbar.chkAutoPlayPlayouts.isSelected()) {
					try {
						playouts = Integer
								.parseInt(Lizzie.frame.toolbar.txtAutoPlayPlayouts.getText().replace(" ", ""));
					} catch (NumberFormatException err) {
					}
				}
				if (Lizzie.frame.toolbar.chkAutoPlayFirstPlayouts.isSelected()) {
					try {
						firstPlayouts = Integer
								.parseInt(Lizzie.frame.toolbar.txtAutoPlayFirstPlayouts.getText().replace(" ", ""));
					} catch (NumberFormatException err) {
					}
				}
//				if((isZen&&Lizzie.board.getHistory().getCurrentHistoryNode().getData().moveNumber<3))
//				{
//					int coords[] = Lizzie.board.convertNameToCoordinates(bestMoves.get(0).coordinate);
//					if ((Lizzie.board.getData().blackToPlay && Lizzie.frame.toolbar.chkAutoPlayBlack.isSelected())
//							|| (!Lizzie.board.getData().blackToPlay
//									&& Lizzie.frame.toolbar.chkAutoPlayWhite.isSelected())) {
//						Lizzie.board.place(coords[0], coords[1]);
//
//					}
//					if (!Lizzie.config.playponder) {
//						if (!(Lizzie.frame.toolbar.chkAutoPlayWhite.isSelected()
//								&& Lizzie.frame.toolbar.chkAutoPlayBlack.isSelected())) {
//							nameCmd();
//						}
//					}
//					return;
//				}
				boolean playNow=false;
				if(playImmediately)
					playNow=true;
				if (firstPlayouts > 0) {
					if (bestMoves.get(0).playouts >= firstPlayouts) {
						playNow=true;
					}
				}
				if (playouts > 0) {
					int sum = 0;
					for (MoveData move : bestMoves) {
						sum += move.playouts;
					}
					if (sum >= playouts) {
						playNow=true;
					}
				}

				if (time > 0) {
					if (System.currentTimeMillis() - startPonderTime >= time) {
						playNow=true;						
					}
				}				
				if(playNow)
				{
					if(Lizzie.frame.isAnaPlayingAgainstLeelaz&&!Lizzie.frame.bothSync)
					{
						if(Lizzie.board.getHistory().getMoveNumber()>=Lizzie.config.anaGameResignStartMove) {
						if(bestMoves.get(0).oriwinrate<Lizzie.config.anaGameResignPercent)
					{
						this.anaGameResignCount++;
					}
						else
							this.anaGameResignCount=0;
						}
					if(this.anaGameResignCount>=Lizzie.config.anaGameResignMove)
					{
						   Lizzie.frame.togglePonderMannul();	
						   Utils.showMsg(oriEnginename+" "+resourceBundle.getString("Leelaz.resign"));
						   return;
						
					}
					}
					MoveData playMove=null;					
					if(!Lizzie.frame.bothSync&&Lizzie.config.enableAnaGameRamdonStart&&Lizzie.board.getHistory().getMoveNumber()<=Lizzie.config.anaGameRandomMove)
						playMove=this.randomBestmove(bestMoves, Lizzie.config.anaGameRandomWinrateDiff,true);
					else
						playMove=bestMoves.get(0);
					
					int coords[] = Lizzie.board.convertNameToCoordinates(playMove.coordinate);
					Lizzie.board.place(coords[0], coords[1]);
					if ((Lizzie.board.getData().blackToPlay && Lizzie.frame.toolbar.chkAutoPlayBlack.isSelected())
							|| (!Lizzie.board.getData().blackToPlay
									&& Lizzie.frame.toolbar.chkAutoPlayWhite.isSelected())) {
						Lizzie.board.place(coords[0], coords[1]);

					}
					if (!Lizzie.config.playponder) {
						if (!(Lizzie.frame.toolbar.chkAutoPlayWhite.isSelected()
								&& Lizzie.frame.toolbar.chkAutoPlayBlack.isSelected())) {
							nameCmd();
						}
					}
				}
			}
		}
	}

	

	

	public void analyzeNextMove(boolean isLastMove) {
		autoAnalysed = true;
		Lizzie.board.getHistory().getCurrentHistoryNode().analyzed=true;
		bestMoves = new ArrayList<>();	
		if(isLastMove)
		{							
			Lizzie.frame.toolbar.stopAutoAna(true,false);
		}
		else {
		Lizzie.board.nextMove(true);	
		}
	}
	
	private void nofityDiffAna() {
		// TODO Auto-generated method stub
		if(this!=Lizzie.leelaz)
			return;
				if (Lizzie.config. autoAnaDiffFirstPlayouts > 0) {
					if (!bestMoves.isEmpty()&&bestMoves.get(0).playouts >= Lizzie.config.autoAnaDiffFirstPlayouts) {		
						Lizzie.board.getHistory().getCurrentHistoryNode().diffAnalyzed=true;						
						return;
					}
				}
				if((isZen&&Lizzie.board.getHistory().getCurrentHistoryNode().getData().moveNumber<3))
				{
					Lizzie.board.getHistory().getCurrentHistoryNode().diffAnalyzed=true;					
					return;
				}
				if (Lizzie.config.autoAnaDiffPlayouts > 0) {
					int sum = 0;
					for (MoveData move : bestMoves) {
						sum += move.playouts;
					}
					if (sum >= Lizzie.config.autoAnaDiffPlayouts) {		
						Lizzie.board.getHistory().getCurrentHistoryNode().diffAnalyzed=true;					
						return;
					}
				}

				if (Lizzie.config.autoAnaDiffTime > 0) {
					long curTime=System.currentTimeMillis();
					if (curTime - startPonderTime >= Lizzie.config.autoAnaDiffTime*1000) {	
						Lizzie.board.getHistory().getCurrentHistoryNode().diffAnalyzed=true;						
						return;
					}
				}
	}
	
	public void notifyAutoAnaAllBranch()   {
		if(this!=Lizzie.leelaz)
			return;
				if(Lizzie.board.getHistory().isBlacksTurn()&&!Lizzie.config.anaBlack)
				{					
					Lizzie.board.getHistory().getCurrentHistoryNode().analyzed=true;
					return;
				}
				if(!Lizzie.board.getHistory().isBlacksTurn()&&!Lizzie.config.anaWhite)
				{					
					Lizzie.board.getHistory().getCurrentHistoryNode().analyzed=true;
					return;
				}
				if (Lizzie.config.autoAnaFirstPlayouts > 0) {
					if (!bestMoves.isEmpty()&&bestMoves.get(0).playouts >= Lizzie.config.autoAnaFirstPlayouts) {		
						Lizzie.board.getHistory().getCurrentHistoryNode().analyzed=true;
						autoAnalysed = true;
						return;
					}
				}
				if((isZen&&Lizzie.board.getHistory().getCurrentHistoryNode().getData().moveNumber<3))
				{
					Lizzie.board.getHistory().getCurrentHistoryNode().analyzed=true;
					autoAnalysed = true;
					return;
				}
				if (Lizzie.config.autoAnaPlayouts > 0) {
					int sum = 0;
					for (MoveData move : bestMoves) {
						sum += move.playouts;
					}
					if (sum >= Lizzie.config.autoAnaPlayouts) {		
						Lizzie.board.getHistory().getCurrentHistoryNode().analyzed=true;
						autoAnalysed = true;
						return;
					}
				}

				if (Lizzie.config.autoAnaTime > 0) {
					long curTime=System.currentTimeMillis();
					if (curTime - startPonderTime >= Lizzie.config.autoAnaTime) {	
						Lizzie.board.getHistory().getCurrentHistoryNode().analyzed=true;
						autoAnalysed = true;
						return;
					}
				}
				}
	
	public void notifyAutoAna()   {
		if(this!=Lizzie.leelaz)
			return;
				if (Lizzie.config.autoAnaEndMove != -1) {
					if (Lizzie.config.autoAnaEndMove < Lizzie.board.getHistory().getData().moveNumber) {
						Lizzie.frame.toolbar.stopAutoAna(true,false);
						return;
					}
				}			
				boolean isLastMove=!Lizzie.board.getHistory().getNext().isPresent();
				if(Lizzie.board.getHistory().isBlacksTurn()&&!Lizzie.config.anaBlack)
				{					
					analyzeNextMove(isLastMove);
					return;
				}
				if(!Lizzie.board.getHistory().isBlacksTurn()&&!Lizzie.config.anaWhite)
				{					
					analyzeNextMove(isLastMove);
					return;
				}
				if (Lizzie.config.autoAnaFirstPlayouts > 0) {
					if (!bestMoves.isEmpty()&&bestMoves.get(0).playouts >= Lizzie.config.autoAnaFirstPlayouts) {		
						analyzeNextMove(isLastMove);
						return;
					}
				}
				if((isZen&&Lizzie.board.getHistory().getCurrentHistoryNode().getData().moveNumber<3))
				{
					analyzeNextMove(isLastMove);
					return;
				}
				if (Lizzie.config.autoAnaPlayouts > 0) {
					int sum = 0;
					for (MoveData move : bestMoves) {
						sum += move.playouts;
					}
					if (sum >= Lizzie.config.autoAnaPlayouts) {		
						analyzeNextMove(isLastMove);
						return;
					}
				}

				if (Lizzie.config.autoAnaTime > 0) {
					long curTime=System.currentTimeMillis();
					if (curTime - startPonderTime >= Lizzie.config.autoAnaTime) {	
						analyzeNextMove(isLastMove);
						return;
					}
				}
				}

	public void genmoveResign(boolean needPass) {	
		//if(resigned)
		//	return;
		if(!bestMoves.isEmpty())
			Lizzie.board.getHistory().getData().tryToSetBestMoves(bestMoves, currentEnginename,true);
		this.resigned=true;				
		if(!this.doublePass&&!this.outOfMoveNum&&(Lizzie.gtpConsole.isVisible()||Lizzie.config.alwaysGtp))
		Lizzie.gtpConsole.addLine(oriEnginename + resourceBundle.getString("Leelaz.resign")+"\n");
		Lizzie.board.updateComment();
		if(needPass)
			Lizzie.board.pass();
		Lizzie.engineManager.stopEngineGame(currentEngineN, false);

	}

	public void pkResign() {
		if (!resigned || isResigning)
			return;
//		if(isManualB) {
//			isManualB=false;
//			blackResignMoveCounts=Lizzie.frame.toolbar.pkResignMoveCounts+1;
//		}
//		else
//		if(isManualW) {
//			isManualW=false;
//			whiteResignMoveCounts=Lizzie.frame.toolbar.pkResignMoveCounts+1;
//		}
		isResigning = true;		
		if(Lizzie.gtpConsole.isVisible()||Lizzie.config.alwaysGtp)
		Lizzie.gtpConsole.addLine(oriEnginename+ resourceBundle.getString("Leelaz.resign")+"\n");
	Lizzie.engineManager.stopEngineGame(currentEngineN, false);
	//resigned = false;
	}

	 
	private void notifyAutoPK(boolean playImmediately) {
		if (played||resigned||Lizzie.frame.toolbar.isPkStop) {
			return;
		}
		if (Lizzie.engineManager.isEngineGame) {
			double curWR = this.bestMoves.get(0).oriwinrate;

			int time =0;			
			int playouts = 0;
			int firstPlayouts = 0;
			int minMove=0;
			int resginMoveCounts=2;
			double resignWinrate=10.0;
			if(currentEngineN==Lizzie.engineManager.engineGameInfo.blackEngineIndex)
			{	time=Lizzie.engineManager.engineGameInfo.timeBlack*1000;
			playouts=Lizzie.engineManager.engineGameInfo.playoutsBlack;
			firstPlayouts=Lizzie.engineManager.engineGameInfo.firstPlayoutsBlack;
			minMove=Lizzie.engineManager.engineGameInfo.blackMinMove;
			resginMoveCounts=Lizzie.engineManager.engineGameInfo.blackResignMoveCounts;
			resignWinrate=Lizzie.engineManager.engineGameInfo.blackResignWinrate;
			}
			else
			{
				time=Lizzie.engineManager.engineGameInfo.timeWhite*1000;
				playouts=Lizzie.engineManager.engineGameInfo.playoutsWhite;
				firstPlayouts=Lizzie.engineManager.engineGameInfo.firstPlayoutsWhite;
				minMove=Lizzie.engineManager.engineGameInfo.whiteMinMove;
				resginMoveCounts=Lizzie.engineManager.engineGameInfo.whiteResignMoveCounts;
				resignWinrate=Lizzie.engineManager.engineGameInfo.whiteResignWinrate;
			}
			
			   if (Lizzie.frame.toolbar.checkGameMaxMove&&Lizzie.board.getHistory().getMoveNumber() > Lizzie.frame.toolbar.maxGanmeMove) {
				   outOfMoveNum=true;
					resigned = true;

				//	pkResign();
				
					nameCmd();
				  return;
			   }
			   if(isZen)
			   {
				   if(Lizzie.board.getHistory().getCurrentHistoryNode().getData().moveNumber<3)
				   playNow=true;
				}
			   if(playImmediately)
				   playNow=true;
			if (firstPlayouts > 0||playNow) {
				if (bestMoves.get(0).playouts >= firstPlayouts||playNow) {
					played = true;
					playNow=false;
					if ((curWR < resignWinrate)
							&& Lizzie.board.getHistory().getMoveNumber() > minMove) {
						if (Lizzie.board.getHistory().isBlacksTurn())
							blackResignMoveCounts = blackResignMoveCounts + 1;
						else
							whiteResignMoveCounts = whiteResignMoveCounts + 1;
					} else {

						if (blackResignMoveCounts > 0 || whiteResignMoveCounts > 0) {
							if (Lizzie.board.getHistory().isBlacksTurn())
								blackResignMoveCounts = 0;
							else
								whiteResignMoveCounts = 0;
						}
					}
					if (blackResignMoveCounts >= resginMoveCounts
							|| whiteResignMoveCounts >= resginMoveCounts) {
						if (bestMoves.get(0).coordinate.equals("pass")) {
							Lizzie.board.pass();
						} else {
							int coords[] = Lizzie.board.convertNameToCoordinates(bestMoves.get(0).coordinate);
							Lizzie.board.place(coords[0], coords[1]);
						}
						resigned = true;
				//		pkResign();					
						nameCmd();
						return;
					}
					
					MoveData playMove=null;
					if(Lizzie.frame.toolbar.isRandomMove&&Lizzie.board.getHistory().getMoveNumber()<=Lizzie.frame.toolbar.randomMove)
						playMove=this.randomBestmove(bestMoves, Lizzie.frame.toolbar.randomDiffWinrate,false);
					else
						playMove=bestMoves.get(0);
					if (playMove.coordinate.equals("pass")) {
						Optional<int[]> passStep = Optional.empty();
						Optional<int[]> lastMove = Lizzie.board.getLastMove();
						if (lastMove == passStep) {
							Lizzie.board.pass();
							doublePass = true;
							resigned = true;
							nameCmdfornoponder();
							return;
						}
						Lizzie.board.pass();
						if ( this.currentEngineN == Lizzie.engineManager.engineGameInfo.blackEngineIndex) {
								Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex)
										.playMoveNoPonder("B", "pass");
							Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex).playMovePonder("B",
									"pass");
//							Lizzie.leelaz = Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex);
							// Lizzie.leelaz.isPondering=true;
							
							// Lizzie.leelaz.played=false;
						}

						else {
							Lizzie.board.pass();							
								Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex)
										.playMoveNoPonder("W", "pass");
							Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex).playMovePonder("W",
									"pass");
//							Lizzie.leelaz = Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex);
							// Lizzie.leelaz.isPondering=true;
							
							// Lizzie.leelaz.played=false;
						}
						return;
					}

					int coords[] = Lizzie.board.convertNameToCoordinates(playMove.coordinate);

					// nameCmd();
					Lizzie.board.place(coords[0], coords[1]);
					if ( this.currentEngineN == Lizzie.engineManager.engineGameInfo.blackEngineIndex) {
							Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex).playMoveNoPonder("B",
									Lizzie.board.convertCoordinatesToName(coords[0], coords[1]));
						Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex).playMovePonder("B",
								Lizzie.board.convertCoordinatesToName(coords[0], coords[1]));

//						Lizzie.leelaz = Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex);
						// Lizzie.leelaz.isPondering=true;
					
						// Lizzie.leelaz.played=false;
					}

					else { //
							// Lizzie.leelaz.isPondering=true;
						Lizzie.board.place(coords[0], coords[1]);					
							Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex).playMoveNoPonder("W",
									Lizzie.board.convertCoordinatesToName(coords[0], coords[1]));
						Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex).playMovePonder("W",
								Lizzie.board.convertCoordinatesToName(coords[0], coords[1]));
//						Lizzie.leelaz = Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex);
						
						// Lizzie.leelaz.played=false;
					}
					return;
				}
			}
			if (playouts > 0) {
				int sum = 0;
				for (MoveData move : bestMoves) {
					sum += move.playouts;
				}
				if (sum >= playouts||(isZen&&sum<-100)) {
					played = true;
					// if(playanyway)
					// playanyway=false;
					if (curWR < resignWinrate
							&& Lizzie.board.getHistory().getMoveNumber() > minMove) {
						if (Lizzie.board.getHistory().isBlacksTurn())
							blackResignMoveCounts = blackResignMoveCounts + 1;
						else
							whiteResignMoveCounts = whiteResignMoveCounts + 1;
					} else {

						if (blackResignMoveCounts > 0 || whiteResignMoveCounts > 0) {
							if (Lizzie.board.getHistory().isBlacksTurn())
								blackResignMoveCounts = 0;
							else
								whiteResignMoveCounts = 0;
						}
					}
					if (blackResignMoveCounts >=resginMoveCounts
							|| whiteResignMoveCounts >= resginMoveCounts) {
//						if (bestMoves.get(0).coordinate.equals("pass")) {
//							Lizzie.board.pass();
//						} else {
//							int coords[] = Lizzie.board.convertNameToCoordinates(bestMoves.get(0).coordinate);
//							Lizzie.board.place(coords[0], coords[1]);
//						}
						resigned = true;
						// pkResign();
						// System.out.println("认输2"+this.currentEngineN);
						nameCmd();

						return;
					}

					MoveData playMove=null;
					if(Lizzie.frame.toolbar.isRandomMove&&Lizzie.board.getHistory().getMoveNumber()<=Lizzie.frame.toolbar.randomMove)
						playMove=this.randomBestmove(bestMoves, Lizzie.frame.toolbar.randomDiffWinrate,false);
					else
						playMove=bestMoves.get(0);
					if (playMove.coordinate.equals("pass")) {
						Optional<int[]> passStep = Optional.empty();
						Optional<int[]> lastMove = Lizzie.board.getLastMove();
						if (lastMove == passStep) {
							Lizzie.board.pass();
							doublePass = true;
							resigned = true;
							nameCmd();
							return;
						}
						Lizzie.board.pass();
						if (this.currentEngineN == Lizzie.engineManager.engineGameInfo.blackEngineIndex) {						
								Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex)
										.playMoveNoPonder("B", "pass");
							Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex).playMovePonder("B",
									"pass");
							// Lizzie.leelaz=Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex);
							// Lizzie.leelaz.isPondering=true;
							
							// Lizzie.leelaz.played=false;
						}

						else {
							Lizzie.board.pass();
								Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex)
										.playMoveNoPonder("W", "pass");
							Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex).playMovePonder("W",
									"pass");
							// Lizzie.leelaz=Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex);
							// Lizzie.leelaz.isPondering=true;
							
							// Lizzie.leelaz.played=false;
						}
						return;
					}

					int coords[] = Lizzie.board.convertNameToCoordinates(playMove.coordinate);

					// nameCmd();
					Lizzie.board.place(coords[0], coords[1]);
					if (this.currentEngineN == Lizzie.engineManager.engineGameInfo.blackEngineIndex) {
							Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex).playMoveNoPonder("B",
									Lizzie.board.convertCoordinatesToName(coords[0], coords[1]));
						Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex).playMovePonder("B",
								Lizzie.board.convertCoordinatesToName(coords[0], coords[1]));
						// Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex).bestMoves
						// = new ArrayList<>();
						// Lizzie.leelaz=Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex);
						// Lizzie.leelaz.isPondering=true;
					

						// Lizzie.leelaz.played=false;
					}

					else {
						// Lizzie.leelaz.isPondering=true;
						Lizzie.board.place(coords[0], coords[1]);
							Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex).playMoveNoPonder("W",
									Lizzie.board.convertCoordinatesToName(coords[0], coords[1]));
						Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex).playMovePonder("W",
								Lizzie.board.convertCoordinatesToName(coords[0], coords[1]));
						// Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex).bestMoves
						// = new ArrayList<>();
						// Lizzie.leelaz=Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex);
						

						// Lizzie.leelaz.played=false;
					}
					return;

				}
			}

			if (time > 0) {
				if (System.currentTimeMillis() - startPonderTime >= time) {
					played = true;
					// if(playanyway)
					// playanyway=false;
					if (curWR < resignWinrate
							&& Lizzie.board.getHistory().getMoveNumber() > minMove) {
						if (Lizzie.board.getHistory().isBlacksTurn())
							blackResignMoveCounts = blackResignMoveCounts + 1;
						else
							whiteResignMoveCounts = whiteResignMoveCounts + 1;
					} else {

						if (blackResignMoveCounts > 0 || whiteResignMoveCounts > 0) {
							if (Lizzie.board.getHistory().isBlacksTurn())
								blackResignMoveCounts = 0;
							else
								whiteResignMoveCounts = 0;
						}
					}
					if (blackResignMoveCounts >= resginMoveCounts
							|| whiteResignMoveCounts >= resginMoveCounts) {
						if (bestMoves.get(0).coordinate.equals("pass")) {
							Lizzie.board.pass();
						} else {
							int coords[] = Lizzie.board.convertNameToCoordinates(bestMoves.get(0).coordinate);
							Lizzie.board.place(coords[0], coords[1]);
						}
						resigned = true;
						// pkResign();
						// System.out.println("认输2"+this.currentEngineN);
						nameCmd();

						return;
					}

					MoveData playMove=null;
					if(Lizzie.frame.toolbar.isRandomMove&&Lizzie.board.getHistory().getMoveNumber()<=Lizzie.frame.toolbar.randomMove)
						playMove=this.randomBestmove(bestMoves, Lizzie.frame.toolbar.randomDiffWinrate,false);
					else
						playMove=bestMoves.get(0);
					if (playMove.coordinate.equals("pass")) {
						Optional<int[]> passStep = Optional.empty();
						Optional<int[]> lastMove = Lizzie.board.getLastMove();
						if (lastMove == passStep) {
							Lizzie.board.pass();
							doublePass = true;
							resigned = true;
							nameCmd();
							return;
						}
						Lizzie.board.pass();
						if (this.currentEngineN == Lizzie.engineManager.engineGameInfo.blackEngineIndex) {
								Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex)
										.playMoveNoPonder("B", "pass");
							Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex).playMovePonder("B",
									"pass");
							// Lizzie.leelaz=Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex);
							// Lizzie.leelaz.isPondering=true;						
							// Lizzie.leelaz.played=false;
						}

						else {
							Lizzie.board.pass();
								Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex)
										.playMoveNoPonder("W", "pass");
							Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex).playMovePonder("W",
									"pass");
							// Lizzie.leelaz=Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex);
							// Lizzie.leelaz.isPondering=true;						
							// Lizzie.leelaz.played=false;
						}
						return;
					}

					int coords[] = Lizzie.board.convertNameToCoordinates(playMove.coordinate);

					// nameCmd();
					if (this.currentEngineN == Lizzie.engineManager.engineGameInfo.blackEngineIndex) {
						Lizzie.board.place(coords[0], coords[1]);
							Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex).playMoveNoPonder("B",
									Lizzie.board.convertCoordinatesToName(coords[0], coords[1]));
						Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex).playMovePonder("B",
								Lizzie.board.convertCoordinatesToName(coords[0], coords[1]));
						// Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex).bestMoves
						// = new ArrayList<>();
						// Lizzie.leelaz=Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex);
						// Lizzie.leelaz.isPondering=true;
						

						// Lizzie.leelaz.played=false;
					}

					else {
						// Lizzie.leelaz.isPondering=true;
						Lizzie.board.place(coords[0], coords[1]);
							Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex).playMoveNoPonder("W",
									Lizzie.board.convertCoordinatesToName(coords[0], coords[1]));
						Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex).playMovePonder("W",
								Lizzie.board.convertCoordinatesToName(coords[0], coords[1]));
						// Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex).bestMoves
						// = new ArrayList<>();
						// Lizzie.leelaz=Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex);						

						// Lizzie.leelaz.played=false;
					}
					return;

				}
			}

		}
	}

	public void nameCmd() {
		if(isKatago)
			sendCommand("stop");
		else
			sendCommand("name");
		Lizzie.frame.menu.toggleEngineMenuStatus(false,false);
	}
	

	public void boardSize(int width, int height) {
		if(width!=height)
			sendCommand("rectangular_boardsize "+width+" "+height);
			else
		sendCommand("boardsize " + width);
		if (firstLoad) {
			Lizzie.board.open(width, height);
			Lizzie.board.getHistory().getGameInfo().setKomi(komi);
			Lizzie.board.getHistory().getGameInfo().DEFAULT_KOMI = (double) komi;
		//	Lizzie.frame.komi = komi + "";
			firstLoad = false;

		}
	}
	
	  public void komi(double komi) {
		    synchronized (this) {
		      sendCommand("komi " + (komi == 0.0 ? "0" : komi));		
		      Lizzie.board.getHistory().getGameInfo().setKomi(komi);
		    //  Lizzie.board.getHistory().getGameInfo().changeKomi(); 
		      Lizzie.board.clearbestmovesafter(Lizzie.board.getHistory().getStart());
		      if (isPondering) ponder();
		    }
		  }
	  
		
	  public void komiNoMenu(double komi) {
		    synchronized (this) {
		      sendCommand("komi " + (komi == 0.0 ? "0" : komi));		
		      Lizzie.board.getHistory().getGameInfo().setKomiNoMenu(komi);
		    //  Lizzie.board.getHistory().getGameInfo().changeKomi(); 
		      Lizzie.board.clearbestmovesafter(Lizzie.board.getHistory().getStart());
		      if (isPondering) ponder();
		    }
		  }

	public void nameCmdfornoponder() {				
			if(isKatago)
			sendCommand("stop");
			else
				sendCommand("name");

	}

	/**
	 * Parse a move-data line of Leelaz output
	 *
	 * @param line output line
	 */
//	private void parseMoveDataLine(String line) {
//		line = line.trim();
//		// ignore passes, and only accept lines that start with a coordinate letter
//		if (line.length() > 0 && Character.isLetter(line.charAt(0)) && !line.startsWith("pass")) {
//			if (!(Lizzie.frame.isPlayingAgainstLeelaz
//					&& Lizzie.frame.playerIsBlack != Lizzie.board.getData().blackToPlay)) {
//				try {
//					bestMovesTemp.add(MoveData.fromInfo(line));
//				} catch (ArrayIndexOutOfBoundsException e) {
//					// this is very rare but is possible. ignore
//				}
//			}
//		}
//	}
	
	private void readError() {
			String line="";
			try {
				while ((line=errorStream.readLine())!= null) {					
					try {
						parseLineForError(line);
					} catch (Exception e) {
					e.printStackTrace();
					}
					 
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}

	private void parseLineForError(String line) {
		// TODO Auto-generated method stub
		if(!this.isLeela0110||Lizzie.frame.isPlayingAgainstLeelaz)
		if(Lizzie.gtpConsole.isVisible()||Lizzie.config.alwaysGtp||!this.isLoaded)
			if(!line.startsWith("inf"))
			Lizzie.gtpConsole.addErrorLine(line+"\n");
		if((isLeela||isSai)&&Lizzie.frame.isPlayingAgainstLeelaz&&canGetSummaryInfo) {
			int k = (Lizzie.config.limitMaxSuggestion > 0&&!Lizzie.config.showNoSuggCircle ? Lizzie.config.limitMaxSuggestion : 361);
			if(bestMovesPrevious.size()<k)
					{
						if (line.contains("->")) {				
						try {	
						MoveData mv = isSai?MoveData.fromSummarySai(line):MoveData.fromSummary(line);
						if (mv != null)
							{mv.order=bestMovesPrevious.size();
							bestMovesPrevious.add(mv);							
							}													
						}
						catch(Exception ex)
						{
							Lizzie.gtpConsole.addLine("genmovepk summary err");
						}				
						}			
				}
					
		}
		if(isLeela0110&&!(Lizzie.engineManager.isEngineGame&&Lizzie.engineManager.engineGameInfo.isGenmove)) {
			 if (line.contains(" -> ")) {
			        if (!isLoaded) {
			          Lizzie.frame.refresh();
			        }
			        isLoaded = true;
			          List<MoveData> bm =  leela0110BestMoves;
			          int k = (Lizzie.config.limitMaxSuggestion > 0&&!Lizzie.config.showNoSuggCircle ? Lizzie.config.limitMaxSuggestion : 361);
			          if (!Lizzie.frame.isPlayingAgainstLeelaz
			              && (bm.size() < k)) {
			        	 MoveData mv=MoveData.fromSummaryLeela0110(line);
			        	 mv.order=bm.size();
			            bm.add(mv);				          
			          }	
			        }
			 else if (isLeela0110 && line.startsWith("=====")) {
				 this.canCheckAlive=true;
			        if (isLeela0110PonderingValid()&&!leela0110BestMoves.isEmpty())
			        {	bestMoves=leela0110BestMoves;
			        if(Lizzie.frame.extraMode==2&&Lizzie.leelaz2!=null&&this==Lizzie.leelaz2)			
					Lizzie.board.getData().tryToSetBestMoves2(bestMoves,currentEnginename,true);
			        else
			          Lizzie.board.getData().tryToSetBestMoves(bestMoves,currentEnginename,true);}
			          leela0110UpdatePonder();
			        Lizzie.frame.refresh(1);
			        Lizzie.frame.updateTitle();
			        if (!this.bestMoves.isEmpty()) {			
						if(Lizzie.config.enginePkPonder)
						{	
							if(!isBackGroundThinking)
							{
								notifyAutoPK(false);
								pkResign();							
						}
						}
						else {
							notifyAutoPK(false);
							pkResign();
						}
			        	  notifyAutoPlay(false);		
			        	  if(Lizzie.config.isAutoAna)
			        	  {
			        		  if(Lizzie.frame.isAutoAnalyzingDiffNode) {
			        			  nofityDiffAna();
			        		  }
			        		  else
			        			  if(Lizzie.config.analyzeAllBranch)
			        		  {
			        			  notifyAutoAnaAllBranch();
			        		  }
			        		  else
			        		  {
			        			  notifyAutoAna();
			        		  }
			        	  }			        	 
					}
			        return;
			      }
			 else {
				 if(Lizzie.gtpConsole.isVisible()||Lizzie.config.alwaysGtp||!this.isLoaded)
						Lizzie.gtpConsole.addErrorLine(line+"\n");
			 }
		}
		if(isZen) {
			if (!Lizzie.frame.isPlayingAgainstLeelaz&&line.startsWith("inf")) {
				this.canCheckAlive=true;
				if((isResponseUpToDate())) {
								if (Lizzie.engineManager.isEngineGame) {
									//Lizzie.frame.subBoardRenderer.reverseBestmoves = false;
									//Lizzie.frame.boardRenderer.reverseBestmoves = false;
									if(Lizzie.config.enginePkPonder)
									{	if((Lizzie.board.getHistory().isBlacksTurn()&&this==Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex))||!Lizzie.board.getHistory().isBlacksTurn()&&this==Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex))
									{
										Lizzie.leelaz = this;
									}
									}
									else
										Lizzie.leelaz = this;
									
								}
								// Clear switching prompt
								//switching = false;

								// Display engine command in the title
								Lizzie.frame.updateTitle();
								
									// This should not be stale data when the command number match
										this.bestMoves = parseInfo(line.substring(5));						
										
									if (!this.bestMoves.isEmpty()) {			
										if(Lizzie.config.enginePkPonder)
										{	
											//if((Lizzie.board.getHistory().isBlacksTurn()&&this==Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex))||!Lizzie.board.getHistory().isBlacksTurn()&&this==Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex))
										if(!isBackGroundThinking)
											{
											//if(isResponseUpToDate())							
												notifyAutoPK(false);
												pkResign();							
										}
										}
										else {
											notifyAutoPK(false);
											pkResign();
										}
							        	  notifyAutoPlay(false);		
							        	  if(Lizzie.config.isAutoAna)
							        	  {
							        		  if(Lizzie.frame.isAutoAnalyzingDiffNode) {
							        			  nofityDiffAna();
							        		  }
							        		  else
							        			  if(Lizzie.config.analyzeAllBranch)
							        		  {
							        			  notifyAutoAnaAllBranch();
							        		  }
							        		  else
							        		  {
							        			  notifyAutoAna();
							        		  }
							        	  }			        	 
									}
									if(!played)
									Lizzie.frame.refresh(1);
									// don't follow the maxAnalyzeTime rule if we are in analysis mode
									if ((!Lizzie.engineManager.isEngineGame&&!Lizzie.config.isAutoAna))
									{
										if(!outOfPlayoutsLimit&&Lizzie.config.limitPlayout&&getTotalPlayouts(bestMoves)>Lizzie.config.limitPlayouts) {
											stopByLimit=true;
											stopByPlayouts=true;
											isPondering = !isPondering;
											nameCmd();
										}
										 else if(Lizzie.config.limitTime&& (System.currentTimeMillis() - startPonderTime) > Lizzie.config.maxAnalyzeTimeMillis)
											{
												stopByLimit=true;
												isPondering = !isPondering;
												nameCmd();
											}
									}								
								}	
								else
								{
									if(Lizzie.config.isAutoAna)
									bestMoves = new ArrayList<>();
								if(Lizzie.config.isAutoAna)
								Lizzie.board.getHistory().getCurrentHistoryNode().getData().tryToClearBestMoves();}								
				return;
							}
				if((Lizzie.frame.isAnaPlayingAgainstLeelaz||(Lizzie.engineManager.isEngineGame&&!Lizzie.engineManager.engineGameInfo.isGenmove))&&line.contains("Nodes:"))
				{				
					if (!this.bestMoves.isEmpty()) {			
						if(Lizzie.config.enginePkPonder)
						{	if((Lizzie.board.getHistory().isBlacksTurn()&&this==Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex))||!Lizzie.board.getHistory().isBlacksTurn()&&this==Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex))
						{
							if(isResponseUpToDate())
							{
								if(!isGamePaused)
								{notifyAutoPK(true);
							pkResign();}
							}
						}
						}
						else {
							if(!isGamePaused) {
							notifyAutoPK(true);
							pkResign();}
						}
						if(Lizzie.frame.isAnaPlayingAgainstLeelaz&&!isGamePaused)
						notifyAutoPlay(true);
					}
				}	
			if (Lizzie.engineManager.isEngineGame&&Lizzie.engineManager.engineGameInfo.isGenmove) {
					if (line.contains("->")) {				
					try {	
					MoveData mv = MoveData.fromSummaryZen(line);
					if (mv != null)
						{mv.order=bestMoves.size();
						bestMoves.add(mv);						
					}
					}
					catch(Exception ex)
					{
						Lizzie.gtpConsole.addLine("genmovepk summary err");
					}				
					}	
				}
			
			if ((Lizzie.frame.isPlayingAgainstLeelaz||isInputCommand)) {
				if (line.contains("->")) {
					int k = (Lizzie.config.limitMaxSuggestion > 0&&!Lizzie.config.showNoSuggCircle ? Lizzie.config.limitMaxSuggestion : 361);
					if(bestMoves.size()<k) {
						MoveData mv = MoveData.fromSummaryZen(line);
					if (mv != null)
											
						{
							mv.order=bestMoves.size();
						bestMoves.add(mv);						
					Lizzie.board.getData().tryToSetBestMoves(bestMoves,currentEnginename,true);
						}					
					}		
				}
			}
		}
		
		
		if(!this.isKatago) {
			if(line.startsWith("NN eval"))
			{
				String[] params = line.trim().split("=");
				 heatwinrate = Double.valueOf(params[1].length()>5?params[1].substring(0, 5):params[1]);
			}
			if(line.startsWith("root eval"))
			{
				String[] params = line.trim().split("=");
				 heatwinrate = Double.valueOf(params[1].length()>5?params[1].substring(0, 5):params[1]);
			}
		
	if(line.endsWith("nodes"))
			{				
				if (!this.bestMoves.isEmpty()) {	
					if(Lizzie.engineManager.isEngineGame&&!Lizzie.engineManager.engineGameInfo.isGenmove)
					{	if((Lizzie.board.getHistory().isBlacksTurn()&&this==Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex))||!Lizzie.board.getHistory().isBlacksTurn()&&this==Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex))
					{
						if(isResponseUpToDate())
						{
							if(!isGamePaused)
							{notifyAutoPK(true);
						pkResign();}
						}
					}}
					if(Lizzie.frame.isAnaPlayingAgainstLeelaz&&!isGamePaused)
					notifyAutoPlay(true);
				}
			}
	if (line.startsWith("| ST")) {
				String[] params = line.trim().split(" ");
				if (params.length == 13) {
					isColorEngine = true;
					if(Lizzie.gtpConsole.isVisible()||Lizzie.config.alwaysGtp)
						Lizzie.gtpConsole.addLine(oriEnginename + ": " + line);
					stage = Integer.parseInt(params[3].substring(0, params[3].length() - 1));
					komi = Float.parseFloat(params[6].substring(0, params[6].length() - 1));	
			        
				}
			} 
		}		
if(!isLoaded) {
	if(line.startsWith("Started OpenCL SGEMM")||line.startsWith("Tuning xGemmDirect"))
	{
		isTuning=true;
	}
}
parseHeatMap(line);
	}
	
	private void parseHeatMap(String line) {
		if (isheatmap) {
			if(isKatago)
			{if(line.startsWith("="))
			{					
				 heatPolicy = new ArrayList<Double>();
				 heatOwnership = new ArrayList<Double>();
				 canheatRedraw=true;
				isCommandLine=true;
				String[] params = line.trim().split(" ");
				if(params.length==3)
				{
					if(params[1].startsWith("symmetry"))
						symmetry = Integer.parseInt(params[2]);
				}					
			}
			if(line.startsWith("whiteWin"))
			{
				String[] params = line.trim().split(" ");
				 heatwinrate = Double.valueOf(params[1]);	
				
			}
			if(line.startsWith("whiteLead"))
			{
				String[] params = line.trim().split(" ");
				heatScore = Double.valueOf(params[1]);
			}
			if(line.startsWith("policy"))
			{
				heatCanGetPolicy=true;
				 heatCanGetOwnership=false;
			}
			if(line.startsWith("whiteOwnership"))
			{
				heatCanGetPolicy=false;
				heatCanGetOwnership=true;
			}
			
			if(heatCanGetPolicy) {
				String[] params = line.trim().split("\\s+");
				if (params.length == Lizzie.board.boardWidth) {
					for (int i = 0; i < params.length; i++)
						{
						try {
						heatPolicy.add((Double.parseDouble(params[i])*1000.0));
						} catch (NumberFormatException ex) {
							heatPolicy.add(0.0);
						}
						}
				}
			}
			
			if(heatCanGetOwnership) {
				String[] params = line.trim().split("\\s+");
				if (params.length == Lizzie.board.boardWidth) {
					boolean blackToPlay=Lizzie.board.getHistory().isBlacksTurn();
					for (int i = 0; i < params.length; i++)
						{
						try {
							heatOwnership.add(blackToPlay?-Double.parseDouble(params[i]):Double.parseDouble(params[i]));
						} catch (NumberFormatException ex) {
							heatOwnership.add(0.0);
						}
						}
				}
				if(heatOwnership.size()==Lizzie.board.boardHeight*Lizzie.board.boardWidth)
				{
					//结束并显示
					if(canheatRedraw)
					{
						canheatRedraw=false;
						if(iskataHeatmapShowOwner)
						Lizzie.frame.drawKataEstimate(this, heatOwnership);
						heatcount = new ArrayList<Integer>();
						for(int i=0;i<heatPolicy.size();i++)
						{
							heatcount.add(heatPolicy.get(i).intValue());
						}
						if(!Lizzie.frame.isShowingHeatmap)
							Lizzie.frame.isShowingHeatmap=true;
						heatCanGetOwnership=false;
					Lizzie.frame.refresh();						
					}					
				}
			}
			}
			else {
				if (line.startsWith(" ") || line.length()>0&&Character.isDigit(line.charAt(0))) {
				try {
					String[] params = line.trim().split("\\s+");
					if (params.length == Lizzie.board.boardWidth) {
						for (int i = 0; i < params.length; i++)
							heatcount.add(Integer.parseInt(params[i]));
					}
				} catch (Exception ex) {
				}
				if(heatcount.size()==Lizzie.board.boardHeight*Lizzie.board.boardWidth)
					Lizzie.frame.refresh();
			}
			if (line.contains("winrate:")) {
				//isheatmap = false;
				if(!Lizzie.frame.isShowingHeatmap)
					Lizzie.frame.isShowingHeatmap=true;
				//Lizzie.frame.refresh();
				if(!isZen) {
				String[] params = line.trim().split(" ");				
				 heatwinrate = Double.valueOf(params[1]);	
				 }		
			}
		}
		}
	}

	/** Continually reads and processes output from leelaz */
	private void read() {
		try {
			int c;
			String line="";
			while (hasUnReadLine||(line=inputStream.readLine())!= null) {
				if(hasUnReadLine)
				{	hasUnReadLine=false;
				line=unReadLine;}
				// while (true) {
				// c = process.getInputStream().read();
				//line.append((char) c);
			//	if ((c == '\n')) {		
					if(getRcentLine&&line.startsWith("= {"))
					{	recentRulesLine=line;
					Lizzie.config.currentKataGoRules=line;
					}
					if (Lizzie.engineManager.isEngineGame && Lizzie.engineManager.engineGameInfo.isGenmove&&isLoaded) {
						try {
							parseLineForGenmovePk(line);
						} catch (Exception e) {
							e.printStackTrace();
						//	Lizzie.gtpConsole.addLine("genmovepkparseline err");
						}

					} else {
						if(startGetCommandList)
						{
							String cmd=line.trim();
							if(!cmd.equals("")&&!cmd.equals("="))
							commandLists.add(cmd);
							}
						try {
							parseLine(line);
						} catch (Exception e) {
							e.printStackTrace();
						}						
					}					
					if(isCommandLine)
					{						
						if(!this.isKatago&&!this.isLeela0110&&Lizzie.frame.isPlayingAgainstLeelaz) {
							  Runnable runnable =
								        new Runnable() {
								          public void run() {
								              try {
								            	  while(!isResponseUpToDate())
								                Thread.sleep(10);			               
								              } catch (InterruptedException e) {
								                // TODO Auto-generated catch block
								                e.printStackTrace();
								              }			    
								          	if(Lizzie.board.getHistory().getCurrentHistoryNode().previous().isPresent()&&!bestMovesPrevious.isEmpty())
												Lizzie.board.getHistory().getCurrentHistoryNode().previous().get().getData()
												.tryToSetBestMoves(bestMovesPrevious,currentEnginename,true);
								          	bestMovesPrevious = new ArrayList<>();
								          	canGetSummaryInfo=false;
								          }
								        };
								    Thread thread = new Thread(runnable);
								    thread.start();
						}
						currentCmdNum ++;
						 
//						if(isModifying&&isResponseUpToDate())
//							setModifyEnd();
						if(currentCmdNum>cmdNumber-1)
							currentCmdNum=cmdNumber-1;
						try {
						trySendCommandFromQueue();
						}
						catch (Exception e) {
							e.printStackTrace();
						}						
					}
					isCommandLine = false;
					//line = new StringBuilder();
//					if(isInfoLine)
//					{
//						if (!this.bestMoves.isEmpty()) {							
//							  notifyAutoPK();	
//				        	  notifyAutoPlay();						        
//						}
//					}
				
				//	isInfoLine=false;
				//} 
//				else if (c == '='||c=='?') {
//					isCommandLine = true;
//				}
			}
			// this line will be reached when engine shuts down
			System.out.println("engine process ended.");
			//process.destroy();
			shutdown();
			if(useJavaSSH)
				javaSSHClosed=true;
			// Do no exit for switching weights
			// System.exit(-1);
		} catch (IOException e) {
			 e.printStackTrace();
		//	System.out.println("读出错");
			// System.exit(-1);
			// read();			
		}
		if(!isNormalEnd)
		{
			started=false;
			//isLoaded=false;
			tryToDignostic(resourceBundle.getString("Leelaz.engineEndUnormalHint"),false);//("打开Gtp窗口(快捷键E)查看报错信息");	
			//LizzieFrame.openMoreEngineDialog();
		}
	}

//	private void stopAutoAna() {			
//		//if (!isClosing) {
//		      			//isClosing=true;			
//		      			Lizzie.frame.toolbar.stopAutoAna();
//		      			//Lizzie.frame.addInput();		      			
//		      			
//		      //			}	 
//	}

	public void setPda(String pda) {
		sendCommand("kata-set-param playoutDoublingAdvantage "+pda);
		try {
		this.pda=Double.parseDouble(pda);
		pdaBeforeGame=Double.parseDouble(pda);
		}
		catch (NumberFormatException e) {
			
		}
	}
	
	public void setGameStatus(boolean isStart) {
		   if (!Lizzie.leelaz.isKatagoCustom || Lizzie.leelaz.isKataGoPda) return;
		if(isStart)
		{
			sendCommand("startGame");
			pdaBeforeGame=pda;
		}
		else {
			sendCommand("stopGame");
			if(Lizzie.config.autoLoadKataEnginePDA)
			{
				this.pda=Double.parseDouble(Lizzie.config.txtKataEnginePDA);
			}else this.pda=pdaBeforeGame;
		}
	}
	/**
	 * Sends a command to command queue for leelaz to execute
	 *
	 * @param command a GTP command containing no newline characters
	 */
	public void sendCommand(String command) {	
		//Lizzie.gtpConsole.addLine(command);		
		if(Lizzie.frame.extraMode==2) {				
			if((command.startsWith("heat")||command.startsWith("kata-raw"))&&!this.isKatago&&Lizzie.leelaz2!=null&&this==Lizzie.leelaz2)
			heatcount = new ArrayList<Integer>();
			if(Lizzie.leelaz2!=null&&this==Lizzie.leelaz2)
				if(this.isLeela0110)
				{
					if(command.startsWith("lz-")||command.startsWith("kata-"))
						this.leela0110Ponder(true);
					return;
				}
				else
				if(this.isKatago&&!Lizzie.leelaz.isKatago)
		{if(command.startsWith("lz-"))
		{
			command="kata-"+command.substring(3);
		}
			if(command.startsWith("heat"))
			{
				command=("kata-raw-nn "+new Random().nextInt(8));
			}
		}
		if(Lizzie.leelaz2!=null&&this==Lizzie.leelaz2&&!this.isKatago&&Lizzie.leelaz.isKatago)
		{	
			if(command.startsWith("kata-raw"))
			{
				command="heatmap";
			}
			if(command.startsWith("kata-"))
			{
				command="lz-"+command.substring(5);
			}		
		
			String[] params = command.trim().split(" ");
			if(params.length>2) {
				if(params[params.length-2].equals("ownership"))
				{
					command=command.substring(0, command.length()-14);
				}
			}				
		}
		}
		synchronized (cmdQueue) {
			// For efficiency, delete unnecessary "lz-analyze" that will be stopped
			// immediately
			cmdNumber++;
			calculateModifyNumber();
			if (!cmdQueue.isEmpty() && (cmdQueue.peekLast().startsWith("lz-analyze")
					|| cmdQueue.peekLast().startsWith("kata-analyze")
							||cmdQueue.peekLast().startsWith("kata-raw")
							||cmdQueue.peekLast().startsWith("heatmap"))) {
				cmdQueue.removeLast();
				cmdNumber--;
			}
			cmdQueue.addLast(command);			
			trySendCommandFromQueue();
			if (Lizzie.frame.isAutocounting) {
				if (command.startsWith("play") || command.startsWith("undo")||command.startsWith("clear")||command.startsWith("boardsize")||command.startsWith("rectan")) {
					Lizzie.frame.zen.sendCommand(command);
					Lizzie.frame.zen.countStones();
					//Lizzie.frame.zen.countStones();
				}
			}
		}
		if(Lizzie.frame.extraMode==2) {
		if(Lizzie.leelaz2!=null&&this!=Lizzie.leelaz2)
		{			
			Lizzie.leelaz2.sendCommand(command);
			Lizzie.leelaz2.startPonderTime=this.startPonderTime;
		}
		}
	}
	
	public void sendCommandNoLeelaz2(String command) {	
		//Lizzie.gtpConsole.addLine(command);
		if(Lizzie.frame.extraMode==2) {	
			if((command.startsWith("heat")||command.startsWith("kata-raw"))&&!this.isKatago&&Lizzie.leelaz2!=null&&this==Lizzie.leelaz2)
			heatcount = new ArrayList<Integer>();
			if(Lizzie.leelaz2!=null&&this==Lizzie.leelaz2&&this.isKatago&&!Lizzie.leelaz.isKatago)
		{if(command.startsWith("lz-"))
		{
			command="kata-"+command.substring(3);
		}
			if(command.startsWith("heat"))
			{
				command=("kata-raw-nn "+new Random().nextInt(8));
			}
		}
		if(Lizzie.leelaz2!=null&&this==Lizzie.leelaz2&&!this.isKatago&&Lizzie.leelaz.isKatago)
		{	
			if(command.startsWith("kata-raw"))
			{
				command="heatmap";
			}
			if(command.startsWith("kata-"))
			{
				command="lz-"+command.substring(5);
			}		
		
			String[] params = command.trim().split(" ");
			if(params.length>2) {
				if(params[params.length-2].equals("ownership"))
				{
					command=command.substring(0, command.length()-14);
				}
			}				
		}
		}
		synchronized (cmdQueue) {
			// For efficiency, delete unnecessary "lz-analyze" that will be stopped
			// immediately
			if (!cmdQueue.isEmpty() && (cmdQueue.peekLast().startsWith("lz-analyze")
					|| cmdQueue.peekLast().startsWith("kata-analyze")
							||cmdQueue.peekLast().startsWith("kata-raw")
							||cmdQueue.peekLast().startsWith("heatmap"))) {
				cmdQueue.removeLast();
			}
			cmdQueue.addLast(command);
			trySendCommandFromQueue();
			if (Lizzie.frame.isAutocounting) {
				if (command.startsWith("play") || command.startsWith("undo")||command.startsWith("clear")||command.startsWith("boardsize")||command.startsWith("rectan")) {
					Lizzie.frame.zen.sendCommand(command);
					Lizzie.frame.zen.countStones();
					//Lizzie.frame.zen.countStones();
				}
			}
		}		
	}

	/** Sends a command from command queue for leelaz to execute if it is ready */
	private void trySendCommandFromQueue() {
		// Defer sending "lz-analyze" if leelaz is not ready yet.
		// Though all commands should be deferred theoretically,
		// only "lz-analyze" is differed here for fear of
		// possible hang-up by missing response for some reason.
		// cmdQueue can be replaced with a mere String variable in this case,
		// but it is kept for future change of our mind.
		synchronized (cmdQueue) {
			if(requireResponseBeforeSend&&!isResponseUpToPreDate())
			{
				return;}
			if (cmdQueue.isEmpty() || (cmdQueue.peekFirst().startsWith("lz-analyze")
					|| cmdQueue.peekFirst().startsWith("kata-analyze")||cmdQueue.peekFirst().startsWith("kata-raw")
					||cmdQueue.peekFirst().startsWith("heatmap")) && !isResponseUpToPreDate()) {				
				return;
			}
			String command = cmdQueue.removeFirst();			
			sendCommandToLeelaz(command);
		}
	}
	

	
	


	/**
	 * Sends a command for leelaz to execute
	 *
	 * @param command a GTP command containing no newline characters
	 */
	
//	private void sendCommandToLeelazWithOutLog(String command) {		
//		if (command.startsWith("fixed_handicap") || (isKatago && command.startsWith("place_free_handicap")))
//			isSettingHandicap = true;
////    if (printCommunication) {
////      System.out.println(currentEnginename+" "+ cmdNumber+" "+ command);
////    }
//		//
//		cmdNumber++;						
//		if (outputStream != null) {
//		try {
//			outputStream.write((command + "\n").getBytes());
//			outputStream.flush();
//		} catch (Exception e) {
//			//e.printStackTrace();
//		}
//		}
////    if(executor!=null&&executor.isShutdown())
////    {	executor = Executors.newSingleThreadScheduledExecutor();
////    executor.execute(this::read);}
//	}
	
	private void sendCommandToLeelaz(String command) {		
		if (command.startsWith("fixed_handicap") || (isKatago && command.startsWith("place_free_handicap")))
			isSettingHandicap = true;
//    if (printCommunication) {
//      System.out.println(currentEnginename+" "+ cmdNumber+" "+ command);
//    }
		//			
//		int commandNumber=cmdNumber;
//		  Runnable runnable =
//			        new Runnable() {
//			          public void run() {
//			        	  try {
//							Thread.sleep(80);
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//			        	  if(cmdNumber==commandNumber)
//			        			setModifyEnd(false);
//			          }
//			        };
//			    Thread thread = new Thread(runnable);
//			    thread.start();		
		if (outputStream != null) {
		try {
			outputStream.write((command + "\n").getBytes());
			outputStream.flush();
		} catch (Exception e) {
			//e.printStackTrace();
		}
		if(Lizzie.config.alwaysGtp||Lizzie.gtpConsole.isVisible())
			Lizzie.gtpConsole.addCommand(command, cmdNumber, oriEnginename);	
		}
//    if(executor!=null&&executor.isShutdown())
//    {	executor = Executors.newSingleThreadScheduledExecutor();
//    executor.execute(this::read);}
	}
	
	//private boolean isModifying() {
//		return Lizzie.board.isModifying||this.isModifying;
//	}

	/** Check whether leelaz is responding to the last command */
	public boolean isResponseUpToDate() {
		// Use >= instead of == for avoiding hang-up, though it cannot happen
		return currentCmdNum >= cmdNumber - 1;//&&currentCmdNum >=ignoreCmdNumber;
	}
	public boolean isResponseUpToPreDate() {
		// Use >= instead of == for avoiding hang-up, though it cannot happen
		return currentCmdNum >= cmdNumber - 2;//&&currentCmdNum >=ignoreCmdNumber;
	}
	
	public void setResponseUpToDate() {
		// Use >= instead of == for avoiding hang-up, though it cannot happen
		currentCmdNum = cmdNumber-1;
	//	ignoreCmdNumber=cmdNumber-1;
	}

	/**
	 * @param color color of stone to play
	 * @param move  coordinate of the coordinate
	 */
	public void playMove(Stone color, String move) {	
		if(!isKatago||isSai)
		{		
			if(move=="pass") {
			if(Lizzie.board.getHistory().getCurrentHistoryNode()!=Lizzie.board.getHistory().getStart())
			{Optional<int[]> lastMove = Lizzie.board.getLastMove();		
			if (!lastMove.isPresent()) {
				this.setModifyEnd();
				return;
			}}}
		}
//		canGetGenmoveInfoGen = true;
	//	getGenmoveInfoPrevious = true;
		synchronized (this) {
			String colorString;
			switch (color) {
			case BLACK:
				colorString = "B";
				break;
			case WHITE:
				colorString = "W";
				break;
			default:
				throw new IllegalArgumentException("The stone color must be B or W, but was " + color.toString());
			}

			sendCommand("play " + colorString + " " + move);
			bestMoves = new ArrayList<>();
			if (Lizzie.frame.isPlayingAgainstLeelaz)
				this.canGetSummaryInfo=true;
//				bestMovesPrevious = new ArrayList<>();
			if ((stopByLimit||isPondering) && !Lizzie.frame.isPlayingAgainstLeelaz)
				if(Lizzie.config.isAutoAna||((Lizzie.config.analyzeBlack&&color==Stone.WHITE)||(Lizzie.config.analyzeWhite&&color==Stone.BLACK)))
					ponder2();
				else {
					nameCmdfornoponder();
					underPonder=true;
				}
		}
	}
	
	public void playMovewithavoid(Stone color, String move) {	
		if (Lizzie.engineManager.isEngineGame) {
			return;
		}
		if(!isKatago||isSai)
		{		
			if(move=="pass") {
				if(Lizzie.board.getHistory().getCurrentHistoryNode().previous().get()!=Lizzie.board.getHistory().getStart())
			{Optional<int[]> lastMove = Lizzie.board.getHistory().getCurrentHistoryNode().previous().get().getData().lastMove;		
			if (!lastMove.isPresent()) {
				this.setModifyEnd();
				return;
			}
			}}
		}
			String colorString;
			switch (color) {
			case BLACK:
				colorString = "B";
				break;
			case WHITE:
				colorString = "W";
				break;
			default:
				throw new IllegalArgumentException("The stone color must be B or W, but was " + color.toString());
			}				
			sendCommand("play " + colorString + " " + move);
			bestMoves = new ArrayList<>();
			if ((stopByLimit||isPondering) && !Lizzie.frame.isPlayingAgainstLeelaz)
				if(Lizzie.config.isAutoAna||((Lizzie.config.analyzeBlack&&color==Stone.WHITE)||(Lizzie.config.analyzeWhite&&color==Stone.BLACK)))
					ponder();
				else {
					nameCmdfornoponder();
					underPonder=true;
				}
	
	}
	
	public void playMoveNoPonder(Stone color, String move) {
		synchronized (this) {
			String colorString;
			switch (color) {
			case BLACK:
				colorString = "B";
				break;
			case WHITE:
				colorString = "W";
				break;
			default:
				throw new IllegalArgumentException("The stone color must be B or W, but was " + color.toString());
			}
			sendCommand("play " + colorString + " " + move);
			//Lizzie.frame.subBoardRenderer.reverseBestmoves = true;
			//Lizzie.frame.boardRenderer.reverseBestmoves = true;
			// bestMoves = new ArrayList<>();
		}
		}	
	

	public void playMoveNoPonder(String colorString, String move) {
		if(Lizzie.config.enginePkPonder)
		{		
			synchronized (this) {
				isBackGroundThinking=true;
				played = false;
				bestMoves = new ArrayList<>();
				sendCommand("play " + colorString + " " + move);
				pkponder();
			}
			pkMoveTime=System.currentTimeMillis() - pkMoveStartTime;
			pkMoveTimeGame=pkMoveTimeGame+pkMoveTime;
			return;
		}
		synchronized (this) {
			sendCommand("play " + colorString + " " + move);
			nameCmdfornoponder();
			//Lizzie.frame.subBoardRenderer.reverseBestmoves = true;
			//Lizzie.frame.boardRenderer.reverseBestmoves = true;
			// bestMoves = new ArrayList<>();
		}
		pkMoveTime=System.currentTimeMillis() - pkMoveStartTime;
		pkMoveTimeGame=pkMoveTimeGame+pkMoveTime;
		}	
	
	

	public void playMovePonder(String colorString, String move) {
		Lizzie.frame.mouseOverCoordinate = Lizzie.frame.outOfBoundCoordinate;
		synchronized (this) {
			played = false;
			if(Lizzie.config.enginePkPonder)			
			bestMoves = new ArrayList<>();
			sendCommand("play " + colorString + " " + move);
			pkponder();
			isBackGroundThinking=false;
		}
		pkMoveStartTime=System.currentTimeMillis();
	}

	public boolean playMoveGenmove(String colorString, String move) {
		//genmoveNode++;
	//	canGetGenmoveInfo = false;
		if(this.resigned)
		{
			this.genmoveResign(false);
			return false;
		}
		synchronized (this) {
			played = false;
			sendCommand("play " + colorString + " " + move);
		}	
		Lizzie.frame.updateTitle();
		return true;
	}



	public void genmove(String color) {
		String command =(this.isKatago?("kata-genmove_analyze " +color+" "+getInterval()): (this.isSai||this.isLeela?("lz-genmove_analyze " +color+" "+getInterval()):("genmove " + color)));		
		/*
		 * We don't support displaying this while playing, so no reason to request it
		 * (for now) if (isPondering) { command = "lz-genmove_analyze " + color + " 10";
		 * }
		 */
		sendCommand(command);
		isThinking = true;
		Lizzie.frame.menu.toggleEngineMenuStatus(false,true);
		//canGetGenmoveInfo = false;
		
		//isPondering = false;
	//	genmovenoponder = false;
	}
	
	public void genmove(String color,boolean useAnalyze) {
		if(useAnalyze)
			genmove(color);
		else
		{String command ="genmove " + color;		
		/*
		 * We don't support displaying this while playing, so no reason to request it
		 * (for now) if (isPondering) { command = "lz-genmove_analyze " + color + " 10";
		 * }
		 */
		sendCommand(command);
		isThinking = true;
		Lizzie.frame.menu.toggleEngineMenuStatus(false,true);
		//canGetGenmoveInfo = false;
		
		//isPondering = false;
		}
	//	genmovenoponder = false;
	}

	public void genmoveForPk(String color) {
		genmoveNode++;
		if(Lizzie.frame.toolbar.isPkStop)
		{
			Lizzie.frame.toolbar.isPkGenmoveStop=true;
			if(color.equals("B"))
		{
			Lizzie.frame.toolbar.isPkStopGenmoveB=true;
		}
		else {
			Lizzie.frame.toolbar.isPkStopGenmoveB=false;
		}
			 Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.whiteEngineIndex).nameCmdfornoponder();
        	 Lizzie.engineManager.engineList.get(Lizzie.engineManager.engineGameInfo.blackEngineIndex).nameCmdfornoponder();
			return;}
		String command =(this.isKatago?("kata-genmove_analyze " +color+" "+getIntervalForGenmovePk()): (this.isSai||this.isLeela?("lz-genmove_analyze " +color+" "+getInterval()):("genmove " + color)));		
		/*
		 * We don't support displaying this while playing, so no reason to request it
		 * (for now) if (isPondering) { command = "lz-genmove_analyze " + color + " 10";
		 * }
		 */
		//bestMoves = new ArrayList<>();
		//canGetGenmoveInfo = true;
		sendCommand(command);
		pkMoveStartTime=System.currentTimeMillis();
		// isThinking = true;

		// isPondering = false;
		// genmovenoponder =false;
	}

//	public void genmove_analyze(String color) {
//		String command = "lz-genmove_analyze " + color + " " + Lizzie.config.analyzeUpdateIntervalCentisec;
//		sendCommand(command);
//		isThinking = true;
//		isPondering = false;
//	}

	public void time_settings() {
		Lizzie.leelaz.sendCommand("time_settings 0 "
				+ Lizzie.config.maxGameThinkingTimeSeconds + " 1");
	}

	public void clear() {
		synchronized (this) {
			sendCommand("clear_board");
			// sendCommand("clear_cache");
			if (isKatago) {
				scoreMean = 0;
				scoreStdev = 0;
			}
			bestMoves = new ArrayList<>();
			if (isPondering)
				ponder();
		}
	}

	public void clearWithoutPonder() {
		synchronized (this) {
			this.notPondering();
			nameCmdfornoponder();		
			sendCommand("clear_board");
			// sendCommand("clear_cache");
			bestMoves = new ArrayList<>();
			// if (isPondering) ponder();
		}
	}

	public void undo() {		
		synchronized (this) {
			sendCommand("undo");
			bestMoves = new ArrayList<>();
			if(isPondering)
					if(Lizzie.config.isAutoAna||((Lizzie.config.analyzeBlack&&Lizzie.board.getHistory().isBlacksTurn())||(Lizzie.config.analyzeWhite&&!Lizzie.board.getHistory().isBlacksTurn())))
					ponder();
				else {
					nameCmdfornoponder();
					underPonder=true;
				}
		}
	}

	public void analyzeAvoid(String type, String color, String coordList, int untilMove) {

		// added for change bestmoves immediatly not wait until totalplayouts is bigger
		// than previous
		// analyze result
		analyzeAvoid(String.format("%s %s %s %d", type, color, coordList, untilMove <= 0 ? 1 : untilMove));
	//	Lizzie.board.getHistory().getData().tryToClearBestMoves();
		Lizzie.board.clearbestmoves();
	}
	
	public void analyzeAvoid(String type,  String coordList, int untilMove) {

//		if (this.isKatago) {
//			return;
//		}
		bestMoves = new ArrayList<>();
		if (!isPondering) {
			isPondering = true;
			startPonderTime = System.currentTimeMillis();
		}
		String parameters= String.format("%s %s %s %d", type, "b", coordList, untilMove <= 0 ? 1 : untilMove);
		parameters=parameters+" "+String.format("%s %s %s %d", type, "w", coordList, untilMove <= 0 ? 1 : untilMove);
			sendCommand(String.format((isKatago?"kata-analyze %d %s"+(Lizzie.config.showKataGoEstimate?" ownership true":"")+(Lizzie.config.showPvVisits?" pvVisits true":""):"lz-analyze %d %s"), getInterval(), parameters));		
			Lizzie.board.clearbestmoves();
	}

	public void analyzeAvoid(String parameters) {
//		if (this.isKatago) {
//			return;
//		}
		// added for change bestmoves immediatly not wait until totalplayouts is bigger
		// than previous
		// analyze result
		bestMoves = new ArrayList<>();
		if (!isPondering) {
			isPondering = true;
			startPonderTime = System.currentTimeMillis();
		}
			sendCommand(String.format((isKatago?"kata-analyze %d %s"+(Lizzie.config.showKataGoEstimate?" ownership true":"")+(Lizzie.config.showPvVisits?" pvVisits true":""):"lz-analyze %d %s"), getInterval(), parameters));		
		//Lizzie.board.getHistory().getData().tryToClearBestMoves();
		Lizzie.board.clearbestmoves();
	}

	/** This initializes leelaz's pondering mode at its current position */
	public void ponder() {
		if(noAnalyze)
			return;
	//	canGetGenmoveInfoGen = false;
//		if(isZen)
//			return;
		isPondering = true;
		underPonder=false;
		startPonderTime = System.currentTimeMillis();	
		if(Lizzie.engineManager.isEngineGame)
			pkMoveStartTime=startPonderTime;
//		if (Lizzie.frame.isheatmap) {
//			Lizzie.leelaz.heatcount.clear();
//			// Lizzie.frame.isheatmap = false;
//		}
		if (!Lizzie.config.playponder && Lizzie.frame.isPlayingAgainstLeelaz) {
			return;
		}				
		if(isheatmap)
		{			
			heatcount = new ArrayList<Integer>();
			sendHeatCommand();
			return;
		}
		 if (isLeela0110) {
		      leela0110Ponder(true);
		      return;
		    }
		int currentmove = Lizzie.board.getcurrentmovenumber();		
		 if (Lizzie.frame.isKeepingForce||RightClickMenu.isKeepForcing) {
			featurecat.lizzie.gui.RightClickMenu.voidanalyze();
		} else {
			RightClickMenu.isTempForcing=false;
			RightClickMenu.allowcoords = "";
			RightClickMenu.avoidcoords = "";
			Lizzie.frame.clearSelectImage();
			//featurecat.lizzie.gui.RightClickMenu.move = 0;
		//	featurecat.lizzie.gui.RightClickMenu.isforcing = false;
			if (this.isKatago) {
				//sendCommand("kata-analyze " + getInterval()+" pvVisits true");
				if (Lizzie.config.showKataGoEstimate)
					sendCommand("kata-analyze " + getInterval() + " ownership true"+(Lizzie.config.showPvVisits?" pvVisits true":""));
				else
					sendCommand("kata-analyze " + getInterval()+(Lizzie.config.showPvVisits?" pvVisits true":""));
			} else {				
				sendCommand("lz-analyze " + getInterval());
			} // until it responds to this, incoming
				// ponder results are obsolete
		}
		Lizzie.frame.menu.toggleEngineMenuStatus(true,false);
	}
	
	public int getInterval() {
		if(isSSH||useJavaSSH)
			return Lizzie.config.analyzeUpdateIntervalCentisecSSH;
		else
			return Lizzie.config.analyzeUpdateIntervalCentisec;
	}
	
	public int getIntervalForGenmovePk() {
		if(isKatago&&Lizzie.config.showPreviousBestmovesInEngineGame)
			return Integer.MAX_VALUE;
		if(isSSH||useJavaSSH)
			return Lizzie.config.analyzeUpdateIntervalCentisecSSH;
		else
			return Lizzie.config.analyzeUpdateIntervalCentisec;
	}
	
	public void ponder2() {
		if(noAnalyze)
			return;
	//	canGetGenmoveInfoGen = false;
//		if(isZen)
//			return;
		isPondering = true;
		underPonder=false;
		startPonderTime = System.currentTimeMillis();
//		if (Lizzie.frame.isheatmap) {
//			Lizzie.leelaz.heatcount.clear();
//			// Lizzie.frame.isheatmap = false;
//		}
		if (!Lizzie.config.playponder && Lizzie.frame.isPlayingAgainstLeelaz) {
			return;
		}
		if(isheatmap)
		{
			heatcount = new ArrayList<Integer>();
			sendHeatCommand();
			return;
		}
		 if (isLeela0110) {
		      leela0110Ponder(true);
		      return;
		    }
		int currentmove = Lizzie.board.getcurrentmovenumber();
		if (Lizzie.frame.isKeepingForce||featurecat.lizzie.gui.RightClickMenu.isKeepForcing) {
			RightClickMenu.voidanalyzeponder();
		} else {
			RightClickMenu.isTempForcing=false;
			RightClickMenu.allowcoords = "";
			RightClickMenu.avoidcoords = "";
			Lizzie.frame.clearSelectImage();
		//	featurecat.lizzie.gui.RightClickMenu.move = 0;
		//	featurecat.lizzie.gui.RightClickMenu.isforcing = false;
			if (this.isKatago) {
				if (Lizzie.config.showKataGoEstimate)
					sendCommand("kata-analyze " + getInterval()+(Lizzie.config.showPvVisits?" pvVisits true":"") + " ownership true");
				else
					sendCommand("kata-analyze " + getInterval()+(Lizzie.config.showPvVisits?" pvVisits true":""));
			} else {
				sendCommand("lz-analyze " + getInterval());
			} // until it responds to this, incoming
				// ponder results are obsolete
		}
		Lizzie.frame.menu.toggleEngineMenuStatus(true,false);
	}
	
	public void pkponder() {
		isPondering = true;
		startPonderTime = System.currentTimeMillis();
		 if (isLeela0110) {
		      leela0110Ponder(true);
		      return;
		    }
			if (this.isKatago) {
				if (Lizzie.config.showKataGoEstimate)
					sendCommand("kata-analyze " + getInterval() +(Lizzie.config.showPvVisits?" pvVisits true":"")+ " ownership true");
				else
					sendCommand("kata-analyze " + getInterval()+(Lizzie.config.showPvVisits?" pvVisits true":""));
			} else {				
				sendCommand("lz-analyze " + getInterval());
			} // until it responds to this, incoming
				// ponder results are obsolete
		
	}


	public void togglePonder() {
		if(underPonder)
		{	ponder();	
		return;
		}
		isPondering = !isPondering;
		//if(isPondering)
		if(stopByPlayouts)
			outOfPlayoutsLimit=true;
		stopByPlayouts=false;
		stopByLimit=false;
		if(Lizzie.frame.isShowingHeatmap)
		{	Lizzie.frame.isShowingHeatmap=false;
		ponder();	
		}
		if (isPondering) {
			ponder();			
		} else {
			nameCmd();
		}	
	}

	/** End the process */
	public void shutdown() {
		leela0110StopPonder();
		if(this.useJavaSSH)
		{
			javaSSH.close();
		}
		else {
		 if (process != null)
		process.destroy();
		}
	}	

	public List<MoveData> getBestMoves() {
	//	synchronized (this) {
			return bestMoves;
	//	}
	}
	public void clearBestMoves() {
		bestMoves = new ArrayList<>();
	}
	
	// public Optional<String> getDynamicKomi() {
	// if (Float.isNaN(dynamicKomi) || Float.isNaN(dynamicOppKomi)) {
	// return Optional.empty();
	// } else {
	// return Optional.of(String.format("%.1f / %.1f", dynamicKomi,
	// dynamicOppKomi));
	// }
	// }
	
//	public void setModifying() {
//		//isModifying=true;	
//	//	ignoreCmdNumber=cmdNumber;
//	}
//	
//	public void setModifyEnd(boolean fromBoard) {
//	//	isModifying=false;		
//	//	if(fromBoard)
//		//	ignoreCmdNumber=cmdNumber-1;
//	}

	public boolean isPondering() {
		return isPondering;
	}

	public void Pondering() {
		isPondering = true;
	}

	public void notPondering() {
		isPondering = false;
	}

	public class WinrateStats {
		public double maxWinrate;
		public int totalPlayouts;
		public double scoreLead;

		public WinrateStats(double maxWinrate, int totalPlayouts,double score) {
			this.maxWinrate = maxWinrate;
			this.totalPlayouts = totalPlayouts;
			this.scoreLead = scoreLead;
		}
	}

	/*
	 * Return the best win rate and total number of playouts. If no analysis
	 * available, win rate is negative and playouts is 0.
	 */
	public WinrateStats getWinrateStats() {
		WinrateStats stats = new WinrateStats(-100, 0,0);
		if (!bestMoves.isEmpty()) {
			// we should match the Leelaz UCTNode get_eval, which is a weighted average
			// copy the list to avoid concurrent modification exception... TODO there must
			// be a better way
			// (note the concurrent modification exception is very very rare)
			// We should use Lizzie Board's best moves as they will generally be the most
			// accurate
			//final List<MoveData> moves = new ArrayList<MoveData>(Lizzie.board.getData().bestMoves);

			// get the total number of playouts in moves
			int totalPlayouts = bestMoves.stream().mapToInt(move -> move.playouts).sum();
			stats.totalPlayouts = totalPlayouts;

			// stats.maxWinrate = bestMoves.get(0).winrate;
			stats.maxWinrate = BoardData.getWinrateFromBestMoves(bestMoves);
			stats.scoreLead=BoardData.getScoreLeadFromBestMoves(bestMoves);
			// BoardData.getWinrateFromBestMoves(moves);
		}

		return stats;
	}	

	/*
	 * initializes the normalizing factor for winrate_to_handicap_stones conversion.
	 */
//	public void estimatePassWinrate() {
//		// we use A1 instead of pass, because valuenetwork is more accurate for A1 on
//		// empty board than a
//		// pass.
//		// probably the reason for higher accuracy is that networks have randomness
//		// which produces
//		// occasionally A1 as first move, but never pass.
//		// for all practical purposes, A1 should equal pass for the value it provides,
//		// hence good
//		// replacement.
//		// this way we avoid having to run lots of playouts for accurate winrate for
//		// pass.
//		playMove(Stone.BLACK, "A1");
//		togglePonder();
//		WinrateStats stats = getWinrateStats();
//
//		// we could use a timelimit or higher minimum playouts to get a more accurate
//		// measurement.
//		while (stats.totalPlayouts < 1) {
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				throw new Error(e);
//			}
//			stats = getWinrateStats();
//		}
//		mHandicapWinrate = stats.maxWinrate;
//		togglePonder();
//		undo();
//		Lizzie.board.clear(false);
//	}

	//public static double mHandicapWinrate = 25;

	/**
	 * Convert winrate to handicap stones, by normalizing winrate by first move pass
	 * winrate (one stone handicap).
	 */
//	public static double winrateToHandicap(double pWinrate) {
//		// we assume each additional handicap lowers winrate by fixed percentage.
//		// this is pretty accurate for human handicap games at least.
//		// also this kind of property is a requirement for handicaps to determined based
//		// on rank
//		// difference.
//
//		// lets convert the 0%-50% range and 100%-50% from both the move and and pass
//		// into range of 0-1
//		double moveWinrateSymmetric = 1 - Math.abs(1 - (pWinrate / 100) * 2);
//		double passWinrateSymmetric = 1 - Math.abs(1 - (mHandicapWinrate / 100) * 2);
//
//		// convert the symmetric move winrate into correctly scaled log scale, so that
//		// winrate of
//		// passWinrate equals 1 handicap.
//		double handicapSymmetric = Math.log(moveWinrateSymmetric) / Math.log(passWinrateSymmetric);
//
//		// make it negative if we had low winrate below 50.
//		return Math.signum(pWinrate - 50) * handicapSymmetric;
//	}

	// public synchronized void addListener(LeelazListener listener) {
	// listeners.add(listener);
	// }

	// Beware, due to race conditions, bestMoveNotification can be called once even
	// after item is
	// removed
	// with removeListener
//	public synchronized void removeListener(LeelazListener listener) {
//		listeners.remove(listener);
//	}

	// private synchronized void notifyBestMoveListeners() {
	// for (LeelazListener listener : listeners) {
	// listener.bestMoveNotification(bestMoves);
	// }
	// }



	public boolean isStarted() {
		return started;
	}
	
	public void clearPDA()
	{
		pda=0.0;
		Lizzie.frame.menu.txtPDA.setText("0.0");
	}
	
	
	//随机落子
	public MoveData randomBestmove(List<MoveData> bestMoves,double diffWinrate,boolean isAutoPlay)
	{
		int maxPlayouts = 0;
		if(Lizzie.config.checkRandomVisits)
		{			
		    for (MoveData move : bestMoves) {
		      if (move.playouts > maxPlayouts) maxPlayouts = move.playouts;
		    }
		}
		double minWinrate=bestMoves.get(0).winrate-diffWinrate;
		List<MoveData> bestMovesTemp = new ArrayList<>();
		bestMovesTemp.add(bestMoves.get(0));
		for(int i=1;i<bestMoves.size();i++)
		{
              if(bestMoves.get(i).winrate>=minWinrate)    
              {
            	  if(isAutoPlay)
            	  {
            		  if(Lizzie.config.anaGameRandomPlayoutsDiff>0)
                		{
                  		  if(bestMoves.get(i).playouts/(float)maxPlayouts>=Lizzie.config.anaGameRandomPlayoutsDiff/100)
                  			  bestMovesTemp.add(bestMoves.get(i));
                		}
            		  bestMovesTemp.add(bestMoves.get(i));
            	  }else {
            	  if(Lizzie.config.checkRandomVisits&&i>0)
          		{
            		  if(bestMoves.get(i).playouts/(float)maxPlayouts>=Lizzie.config.percentsRandomVisits/100)
            			  bestMovesTemp.add(bestMoves.get(i));
          		}
            	  else
            	  bestMovesTemp.add(bestMoves.get(i));}
              }
		}
		    Random random = new Random();
		         int n = random.nextInt(bestMovesTemp.size());		         
		return bestMovesTemp.get(n);
	}

	public boolean isLoaded() {
		return isLoaded;
	}
	
	public void tryToDignostic(String message,boolean isModal) {		
			Lizzie.engineManager.clearEngineGame();		
			if(engineFailedMessage!=null&&engineFailedMessage.isVisible())
				return;
		engineFailedMessage=new EngineFailedMessage(commands,engineCommand,message,!useJavaSSH&&OS.isWindows());	
		engineFailedMessage.setModal(isModal);
		engineFailedMessage.setVisible(true);		
	}

//	public String currentWeight() {
//		return currentWeight;
//	}
//
//	public String currentShortWeight() {
//		if (currentWeight != null && currentWeight.length() > 18) {
//			return currentWeight.substring(0, 16) + "..";
//		}
//		return currentWeight;
//	}

//	public boolean switching() {
//		return switching;
//	}

	public int currentEngineN() {
		return currentEngineN;
	}

	public String engineCommand() {
		return this.engineCommand;
	}

//	public void toggleGtpConsole() {
//		gtpConsole = !gtpConsole;
//	}
//	
	private void setLeelaSaiEnginePara() {
        if (Lizzie.config.chkLzsaiEngineMem&&Lizzie.config.autoLoadLzsaiEngineMem)
            sendCommand(
                "lz-setoption name Maximum Memory Use (MiB) value "
                    + Lizzie.config.txtLzsaiEngineMem);

          if (Lizzie.config.chkLzsaiEngineVisits&&Lizzie.config.autoLoadLzsaiEngineVisits)
            sendCommand(
                "lz-setoption name Visits value " + Lizzie.config.txtLzsaiEngineVisits);
          
          if (Lizzie.config.chkLzsaiEngineLagbuffer&&Lizzie.config.autoLoadLzsaiEngineLagbuffer)
              sendCommand(
                  "lz-setoption name Lagbuffer value " + Lizzie.config.txtLzsaiEngineLagbuffer);
          
          if (Lizzie.config.chkLzsaiEngineResign&&Lizzie.config.autoLoadLzsaiEngineResign)
              sendCommand(
                  "lz-setoption name Resign Percentage value " + Lizzie.config.txtLzsaiEngineResign);
	}
	
	private void setKataEnginePara() {
        if (Lizzie.config.autoLoadKataEnginePDA)
           setPda(Lizzie.config.txtKataEnginePDA);

        if (Lizzie.config.autoLoadKataEngineWRN)
            sendCommand(
                "kata-set-param analysisWideRootNoise "
                    + Lizzie.config.txtKataEngineWRN);
        
        if(Lizzie.config.autoLoadKataEngineThreads)
        	 Lizzie.leelaz.sendCommand(
                     "kata-set-param numSearchThreads " + Lizzie.config.txtKataEngineThreads);	
        
//        if (Lizzie.config.autoLoadKataEngineRPT)
//            sendCommand(
//                "kata-set-param rootPolicyTemperature "
//                    + Lizzie.config.txtKataEngineRPT);
         }
	
	public void setHeatmap() {
	 	  Lizzie.frame.isShowingHeatmap=true;
        isheatmap = true;
        heatcount = new ArrayList<Integer>();
        heatPolicy = new ArrayList<Double>();
        heatOwnership = new ArrayList<Double>();
	}

	public void toggleHeatmap(boolean bySpace) {
		// TODO Auto-generated method stub		
		if(Lizzie.engineManager.isEmpty)
			{Lizzie.frame.togglePolicy();
		return;}
		Lizzie.frame.isShowingPolicy = false;
		if(isKatago)
			Lizzie.frame.clearKataEstimate();
		if((isKatago&&!bySpace)||(Lizzie.config.extraMode==2&&Lizzie.leelaz2!=null&&Lizzie.leelaz2.isKatago))
		{
			if(isheatmap)
			{if(iskataHeatmapShowOwner)
			{
				  Lizzie.frame.isShowingHeatmap=!Lizzie.frame.isShowingHeatmap;
			        isheatmap = Lizzie.frame.isShowingHeatmap;
			        iskataHeatmapShowOwner=false;
			}
			else
			{
				iskataHeatmapShowOwner=true;
			}}
			else
			{
				Lizzie.frame.isShowingHeatmap=!Lizzie.frame.isShowingHeatmap;
		        isheatmap = Lizzie.frame.isShowingHeatmap;
			}
		}
		else {
        Lizzie.frame.isShowingHeatmap=!Lizzie.frame.isShowingHeatmap;        
        isheatmap = Lizzie.frame.isShowingHeatmap;        	
        iskataHeatmapShowOwner=false;
		}
        heatcount = new ArrayList<Integer>();
        heatPolicy = new ArrayList<Double>();
        heatOwnership = new ArrayList<Double>();
		if(isheatmap)
		{ 
			sendHeatCommand();
		}
		else
		{
			Lizzie.board.clearBestHeatMove();			
			if(isPondering){
			ponder();
			
		}
			Lizzie.frame.handleAfterDrawGobanBottom();
		}	
		if(Lizzie.config.extraMode==2&&Lizzie.leelaz2!=null)
			Lizzie.leelaz2.toggleHeatmapSub(bySpace);
	}
	
	public void toggleHeatmapSub(boolean bySpace) {
		// TODO Auto-generated method stub			
		if(isKatago&&!bySpace)
		{
			if(isheatmap)
			{if(iskataHeatmapShowOwner)
			{
				//  Lizzie.frame.isShowingHeatmap=!Lizzie.frame.isShowingHeatmap;
			        isheatmap = Lizzie.frame.isShowingHeatmap;
			        iskataHeatmapShowOwner=false;
			}
			else
			{
				iskataHeatmapShowOwner=true;
			}}
			else
			{
			//	Lizzie.frame.isShowingHeatmap=!Lizzie.frame.isShowingHeatmap;
		        isheatmap = Lizzie.frame.isShowingHeatmap;
			}
		}
		else {
      //  Lizzie.frame.isShowingHeatmap=!Lizzie.frame.isShowingHeatmap;
        isheatmap = Lizzie.frame.isShowingHeatmap;
        iskataHeatmapShowOwner=false;
		}
        heatcount = new ArrayList<Integer>();
        heatPolicy = new ArrayList<Double>();
        heatOwnership = new ArrayList<Double>();
		if(isheatmap)
		{ 
			//sendHeatCommand();
		}
		else
		{
			Lizzie.board.clearBestHeatMove();
			if(isKatago)
				Lizzie.frame.clearKataEstimate();
			if(isPondering){
			ponder();
			
		}
			Lizzie.frame.handleAfterDrawGobanBottomSub();
		}	
	}
	
	
	private void sendHeatCommand()
	{
		if(isKatago)
		{
		sendCommand("kata-raw-nn "+new Random().nextInt(8));
		}
	else
		sendCommand("heatmap");
	}

	
	public void getSuicidalScadule() {
		getRcentLine=true;
		  Runnable runnable =
			        new Runnable() {
			          public void run() {
			              try {
			                Thread.sleep(3000);			               
			              } catch (InterruptedException e) {
			                // TODO Auto-generated catch block
			                e.printStackTrace();
			              }			    
			              getSuicidalAndRules();
			            Lizzie.leelaz.getRcentLine = false;		
			          }
			        };
			    Thread thread = new Thread(runnable);
			    thread.start();
	}
	
	public void getSuicidalAndRules() {
		usingSpecificRules=-1;
		if (recentRulesLine.equals("")) {
			canSuicidal=false;			
		    } else {
		    	 try {
		      String line = recentRulesLine;
		      JSONObject jo = new JSONObject(new String(line.substring(2)));
		      if (jo.optBoolean("suicide", false)) 
		    	  canSuicidal=true;
		      else
		    	  canSuicidal=false;		      
		      if(jo.optString("scoring", "").contentEquals("AREA")&&jo.optString("ko", "").contentEquals("POSITIONAL")&&
		    		  jo.optBoolean("suicide", false)&&jo.optString("tax", "").contentEquals("NONE")&&jo.optString("whiteHandicapBonus", "").contentEquals("N")&&!jo.optBoolean("hasButton", true)) {
		    	  usingSpecificRules=4;//tt规则
		      }
		      else if(jo.optString("scoring", "").contentEquals("AREA")&&jo.optString("tax", "").contentEquals("NONE")&&!jo.optBoolean("hasButton", true)) {
		    	  usingSpecificRules=1; //中国规则
		      }
		      else if(jo.optString("scoring", "").contentEquals("AREA")&&jo.optString("tax", "").contentEquals("ALL")&&!jo.optBoolean("hasButton", true)) {
		    	 usingSpecificRules=2; //中古规则
		      }
		      else if(jo.optString("scoring", "").contentEquals("TERRITORY")&&jo.optString("tax", "").contentEquals("SEKI")) {
		    	 usingSpecificRules=3; //日本规则
		      }
		      else   if(jo.optString("scoring", "").contentEquals("AREA")||jo.optString("scoring", "").contentEquals("TERRITORY")){
		    	 usingSpecificRules=5; //其他规则
		      }
		    	 } 
		    	 catch (Exception e) {
		              }	
		    }
	}
	
	private void leela0110Ponder(boolean first) {
		if(first)
	    	if(Lizzie.frame.extraMode==2) {
	    		if(Lizzie.leelaz2!=null&&this!=Lizzie.leelaz2)
	    		{			
	    			Lizzie.leelaz2.sendCommand("lz-analyze " + getInterval());
	    		}
	    		}
	    synchronized (this) {	    
	      if (leela0110PonderingBoardData != null) return;
	      leela0110PonderingBoardData = Lizzie.board.getData();
	      leela0110BestMoves = new ArrayList<>();
	      sendCommandNoLeelaz2("time_left b 0 0");
	      leela0110PonderingTimer = new Timer();
	      leela0110PonderingTimer.schedule(
	          new TimerTask() {
	            public void run() {
	            	sendCommandNoLeelaz2("name");
	            }
	          },
	          LEELA0110_PONDERING_INTERVAL_MILLIS);
	    }
	  }

	  public void leela0110StopPonder() {
	    if (leela0110PonderingTimer != null) {
	      leela0110PonderingTimer.cancel();
	      leela0110PonderingTimer = null;
	    }
	    leela0110PonderingBoardData = null;
	  }

	  private void leela0110UpdatePonder() {
	    leela0110StopPonder();
	    if (isPondering) leela0110Ponder(false);
	  }

	  private boolean isLeela0110PonderingValid() {
	    return leela0110PonderingBoardData == Lizzie.board.getData();
	  }
	  
	  public int getBestMovesPlayouts() {
		  int totalPlayouts=0;
		  for(MoveData move:bestMoves)
			  totalPlayouts+=move.playouts;
		  return totalPlayouts;
	  }
	  
	  public boolean isStopPonderingByLimit() {
		  return stopByLimit;
	  }
	  
	  public long getStartPonderTime() {
		  return startPonderTime;
	  }
	  
	  private int getTotalPlayouts(List<MoveData> bestMoves) {
		  int totalPlayouts=0;
		  for(MoveData move:bestMoves)
			  totalPlayouts+=move.playouts;
		  return totalPlayouts;
	  }

	public synchronized void modifyStart() {
		// TODO Auto-generated method stub
		this.cmdNumber++;
		this.modifyNumber++;
	}
	
	public synchronized void setModifyEnd() {
		// TODO Auto-generated method stub
		cmdNumber-=modifyNumber;
		modifyNumber=0;
	}

	private synchronized void calculateModifyNumber() {
		// TODO Auto-generated method stub
		cmdNumber-=modifyNumber;
		modifyNumber=0;
	}
}

