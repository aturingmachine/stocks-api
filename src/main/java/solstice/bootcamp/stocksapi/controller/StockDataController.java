package solstice.bootcamp.stocksapi.controller;


import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solstice.bootcamp.stocksapi.model.AggregateData;
import solstice.bootcamp.stocksapi.repository.StockDataRepository;

import java.io.IOException;

@RestController
@RequestMapping("/stocks")
public class StockDataController {
  private final StockDataRepository stockDataRepository;

  public StockDataController(StockDataRepository stockDataRepository) {
    this.stockDataRepository = stockDataRepository;
  }

  @PostMapping("/load")
  public ResponseEntity load() {
    try {
      return new ResponseEntity<>(stockDataRepository.save(), HttpStatus.CREATED);
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.toString());
    }
  }

  @GetMapping("")
  public ResponseEntity getAll() {
    try {
      return new ResponseEntity<>(stockDataRepository.getAll(), HttpStatus.OK);
    } catch (DataAccessException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.toString());
    }
  }


  @GetMapping("/{type}/{symbol}/{date}")
  public ResponseEntity getAggregateData(
      @PathVariable("type") String type,
      @PathVariable("symbol") String symbol,
      @PathVariable("date") String date) {

    AggregateData data;

    try {
      if (type.equals("date")) {
        data = stockDataRepository.compileDataDate(symbol, date);
      } else if (type.equals("month")) {

        if (Integer.parseInt(date) > 12 || Integer.parseInt(date) < 1) {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body("Month out of scope, month must be < 13 && > 0");
        }

        data = stockDataRepository.compileDataDate(symbol, formatDate(date));
      } else {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("Aggregation Format Unsupported, try month or date");
      }

      data.setType(type);
      data.setDateRequested(date);

      return new ResponseEntity<>(data, HttpStatus.OK);

    } catch (EmptyResultDataAccessException | NullPointerException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("Data could not be found for the specified company and date");
    }
  }

  private String formatDate(String date) {
    if (date.length() < 2) {
      date = "0" + date;
    }

    date = "%-" + date + "-";

    return date;
  }
}
