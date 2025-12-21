package com.netroyale.server.controller;

import com.netroyale.server.model.Score;
import com.netroyale.server.repo.ScoreRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scores")
public class ScoreController {

    private final ScoreRepository repo;

    public ScoreController(ScoreRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Score> getAll() {
        return repo.all();
    }

    @PostMapping
    public ResponseEntity<Score> postScore(@RequestBody Score s) {
        if (s == null || s.getPlayer() == null) return ResponseEntity.badRequest().build();
        Score saved = repo.save(s);
        return ResponseEntity.ok(saved);
    }
}
