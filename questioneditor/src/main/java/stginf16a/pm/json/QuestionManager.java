package stginf16a.pm.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import stginf16a.pm.questions.Category;
import stginf16a.pm.questions.Question;
import stginf16a.pm.wrapper.QuestionWrapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Czichotzki on 23.05.2017.
 */
public class QuestionManager {
    private Project project;
    private ObjectMapper mapper;

    public QuestionManager(Project project) {
        this.project = project;
        this.mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    public Category loadCategory(ProjectCategory category) throws IOException {
        Category cat = new Category(category.getCategoryName(), loadQuestions(category));
        category.setCategory(cat);
        return cat;
    }

    public void loadProject() throws IOException {
        for (ProjectCategory category :
                project.getCategories()) {
            loadCategory(category);
        }
    }

    public void saveProject() throws IOException {
        for(ProjectCategory category: project.getCategories()){
            saveCategory(category);
        }
    }

    public void saveCategory(ProjectCategory category) throws IOException {
        File f = new File(project.getPath(category));

        for (QuestionWrapper q :
                category.getCategory().getQuestions()) {
            if(q.isChanged()) {
                saveQuestion(q, f);
                q.setChanged(false);
            }
        }
    }

    private List<QuestionWrapper> loadQuestions(ProjectCategory category) throws IOException {
        List<QuestionWrapper> result = new ArrayList<>();
        File dir = new File(project.getPath(category));
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        for (File file :
                files != null ? files : new File[0]) {
            QuestionWrapper q = loadQuestion(file);
            q.getOriginal().setCategoryName(category.getCategoryName());
            result.add(q);
        }
        return result;
    }

    private QuestionWrapper loadQuestion(File file) throws IOException {
        Question question = mapper.readValue(file, Question.class);
        String id = file.getName().replace(".json", "");
        question.setId(UUID.fromString(id));
        return new QuestionWrapper(question);
    }

    private void saveQuestion(QuestionWrapper question, File file) throws IOException {
        File f = new File(file.getAbsolutePath() + File.separator + question.getOriginal().getId().toString() + ".json");
        mapper.writeValue(f, question.getOriginal());
    }

    public void saveQuestion(QuestionWrapper question, ProjectCategory category) throws IOException {
        if(question.isChanged()) {
            File f = new File(project.getPath(category));
            saveQuestion(question, f);
            question.setChanged(false);
        }
    }

}