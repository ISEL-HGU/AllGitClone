package edu.handong.isel.allgitclone.act;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import edu.handong.isel.allgitclone.control.CmdOptions;


public class ActivityControlUnit {

	public void run(CmdOptions cmdOptions) throws InterruptedException, IOException {
		HashMap<String, String> repoOpt = cmdOptions.getRepoOpt();
		HashMap<String, String> commitOpt = cmdOptions.getCommitOpt();
		HashSet<String> finalResult = new HashSet<>();
		
		double dv;
		int iv;
		int pages = 1;
		
		
		/*
		 * First work : search repositories
		 */
		
		RepoActivity searchRepo = new RepoActivity(cmdOptions.getAuthToken());
		HashSet<String> repoResult = searchRepo.getRepoResult();
		
		while(!searchRepo.isBlank()) {
			
			while (pages != 11 && !searchRepo.isBlank()) {
				
				//random sleep time to 1~3s
				dv = Math.random();
				iv = (int)(dv * 2000) + 1;
				Thread.sleep(iv);
				
				repoOpt.replace("page", String.valueOf(pages));
				
				searchRepo.start(repoOpt);
				
				System.out.println("current page : " + pages);
				System.out.println(repoOpt.get("q"));

				if (!searchRepo.isBlocked())
					pages++;
				
			}
			
			cmdOptions.changeRepoUpdate(repoOpt, searchRepo.lastDate());
			pages = 1;
		}
		
		
		System.out.println(repoResult.size() + " results are stored.\n");
		
		
		
		/*
		 * Second work : search commit
		 */
	
		
		CommitActivity searchCommit = new CommitActivity(cmdOptions.getAuthToken());
		String originQuery = "";
		pages = 1;
		
		double progress = 0, percentage = 0;
		
		for (String query : repoResult)  {
			
			dv = Math.random();
			iv = (int)(dv * 2000) + 1;
			Thread.sleep(iv);
			
			if (!searchCommit.isBlocked()) {
			
				if (!commitOpt.get("q").contains("repo:")) {
					commitOpt.replace("q", commitOpt.get("q") + " repo:" + query);
					originQuery = query;
				}

				else {
					String base = commitOpt.get("q");
					base = base.replace(originQuery, query);
					commitOpt.replace("q", base);
					originQuery = query;
				}
				
				percentage = progress / repoResult.size() * 100.0;
				
				System.out.println(String.format("%.1f", percentage) + "% work completed.");
				progress++;
				searchCommit.start(commitOpt, finalResult);
			}
			
			else {
				
				while (searchCommit.isBlocked()) {
					searchCommit.start(commitOpt, finalResult);
					Thread.sleep(iv);
				}
				
			}
		}
		
		
		System.out.println(finalResult.size() + " results are finally stored.\n");
		
		
		Date today = new Date();
		SimpleDateFormat curTime = new SimpleDateFormat("hh-mm-ss");
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(curTime.format(today) + "Result.csv"), true));
		PrintWriter pw = new PrintWriter(bw, true);
		
		for (String result : finalResult) {
			pw.write(result + "," + "\n");
			pw.flush();
		}
		
		System.out.println("All results are stored in CommitResult.csv");
		pw.close();
	}
}