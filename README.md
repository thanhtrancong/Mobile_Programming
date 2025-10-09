# ...existing code...

# Step-by-step guide to push code to GitHub (to a branch)

1. Open your terminal and navigate to your project directory:
   cd /Users/macbook/AndroidStudioProjects/MyTopTen

2. Initialize git (if not already done):
   git init

3. Add all files to staging:
   git add .

4. Commit your changes:
   git commit -m "Initial commit"

5. Create a new repository on GitHub (https://github.com/new) and copy the repository URL.

6. Add the remote origin:
   git remote add origin https://github.com/your-username/your-repo-name.git

7. Create and switch to your branch (replace 'feature-branch' with your branch name):
   git checkout -b feature-branch

8. Push your code to GitHub on that branch:
   git push -u origin feature-branch

# How to create a GitHub repository using the terminal

1. Install GitHub CLI if you haven't already:
   https://cli.github.com/

2. Authenticate with GitHub CLI:
   gh auth login

3. Create a new repository (replace 'your-repo-name' as needed):
   gh repo create your-repo-name --public --source=. --remote=origin --push

# Troubleshooting: error "failed to push some refs"

This error usually means your local branch is behind the remote branch or there are conflicts.

Try these steps:

1. Fetch and merge changes from the remote:
   git pull origin feature-branch

2. Resolve any merge conflicts if prompted.

3. After merging, push again:
   git push origin feature-branch

If you want to force push (overwrites remote changes, use with caution):
   git push -f origin feature-branch

# Troubleshooting: error "fatal: couldn't find remote ref <branch-name>"

This error means the branch does not exist on the remote yet.

Solution:
1. Make sure you are on the correct local branch:
   git checkout lab05_mytop3

2. Push the branch for the first time:
   git push -u origin lab05_mytop3

# Troubleshooting: error "error: pathspec 'lab05_mytop3' did not match any file(s) known to git"

This error means the branch 'lab05_mytop3' does not exist locally.

Solution:
1. Create and switch to the branch:
   git checkout -b lab05_mytop3

2. Now push the branch to remote:
   git push -u origin lab05_mytop3
# Mobile_Programming

# Simple High Scores Program (Top 3)

## Requirements

- Input player name and score.
- Display top 3 players with highest scores, sorted descending.
- If a new score is lower than all current top 3, do not add.
- If a new score is higher than any in top 3, add it and remove the lowest.
- Persist high scores so they remain after closing and reopening the app.

## Implementation Steps (Android/Java)

1. **UI Design**
   - Create input fields for player name and score.
   - Add a button to submit the score.
   - Display a list of top 3 high scores.

2. **Data Structure**
   - Use a list or array to store up to 3 high score entries (name + score).

3. **Logic**
   - On submit, check if the new score is higher than any in the current top 3.
   - If yes, insert the new score, sort the list descending, and keep only top 3.
   - If no, ignore the new score.

4. **Persistence**
   - Use SharedPreferences (for Android) to save and load the high scores.

5. **Sample Code Outline**

```java
// MainActivity.java
// ...existing imports...

public class MainActivity extends AppCompatActivity {
    // ...existing code...
    private List<ScoreEntry> highScores = new ArrayList<>();
    // ...existing code...

    // Load high scores from SharedPreferences
    private void loadHighScores() {
        // ...load logic...
    }

    // Save high scores to SharedPreferences
    private void saveHighScores() {
        // ...save logic...
    }

    // Add new score
    private void addScore(String name, int score) {
        // ...add logic: insert, sort, keep top 3...
    }
    // ...existing code...
}
```

6. **ScoreEntry Class**
```java
// ScoreEntry.java
public class ScoreEntry {
    public String name;
    public int score;
    // ...constructor, getters, setters...
}
```

7. **Persistence Example**
   - Use JSON or comma-separated values to store the list in SharedPreferences.

8. **Display**
   - Update the UI list whenever high scores change.
