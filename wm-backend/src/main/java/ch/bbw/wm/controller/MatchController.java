package ch.bbw.wm.controller;

import ch.bbw.wm.dto.ResultRequest;
import ch.bbw.wm.entity.Match;
import ch.bbw.wm.factory.MatchView;
import ch.bbw.wm.factory.MatchViewFactory;
import ch.bbw.wm.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;
    private final MatchViewFactory matchViewFactory;

    @Autowired
    public MatchController(MatchService matchService, MatchViewFactory matchViewFactory) {
        this.matchService = matchService;
        this.matchViewFactory = matchViewFactory;
    }

    @GetMapping
    public List<Match> getMatches(@RequestParam(required = false) String group) {
        if (group != null) {
            return matchService.getMatchesByGroup(group);
        }
        return matchService.getAllMatches();
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<MatchView> getMatchView(@PathVariable Long id) {
        return matchService.getAllMatches().stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .map(matchViewFactory::createView)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/result")
    public ResponseEntity<Match> updateResult(@PathVariable Long id,
                                              @RequestBody ResultRequest request) {
        Optional<Match> updated =
                matchService.updateResult(id, request.homeGoals(), request.awayGoals());
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
