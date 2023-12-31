package com.springboot.board.service;

import com.springboot.board.dto.BoardDTO;
import com.springboot.board.entity.BaseEntity;
import com.springboot.board.entity.BoardEntity;
import com.springboot.board.entity.BoardFileEntity;
import com.springboot.board.repository.BoardFileRepository;
import com.springboot.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// service에서 하는 일
// Controller가 호출에서 받은 데이터 -> Repository로 넘겨줄 때 : DTO -> Entity로 변환(Entity class에서 작업)
// 또는
// DB 데이터 조회시 Repository에서 받은 데이터 -> Controller로 return 시 : 2. Entity -> DTO로 변환(DTO class에서 작업)

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardFileRepository boardFileRepository;

    public void save(BoardDTO boardDTO) throws IOException {
        // 파일 첨부 여부에 따라 로직 분리
        if (boardDTO.getBoardFile().isEmpty()) {
            // 첨부 파일 없음.
            BoardEntity boardEntity = BoardEntity.toSaveEntity(boardDTO);
            boardRepository.save(boardEntity);
        } else {
            // 첨부 파일 있음.

            /* 단일 첨부 파일 처리
            // 1. DTO에 담긴 파일을 꺼냄
            MultipartFile boardFile = boardDTO.getBoardFile();

            // 2. 파일의 이름 가져옴
            String originalFilename = boardFile.getOriginalFilename();

            // 3. 서버 저장용 이름을 만듦
            // 내사진.jpg -> 839798375892_내사진.jpg
            String storedFileName = System.currentTimeMillis() + "_" + originalFilename;

            // 4. 저장 경로 설정
            String savePath = "C:/Users/hanyo/Desktop/project/[3-2]Clug_Server_Study" + storedFileName; // C:/springboot_img/9802398403948_내사진.jpg

            // 5. 해당 경로에 파일 저장
            boardFile.transferTo(new File(savePath));

            // 6. board_table에 해당 데이터 save 처리
            BoardEntity boardEntity = BoardEntity.toSaveFileEntity(boardDTO);
            Long savedId = boardRepository.save(boardEntity).getId();
            BoardEntity board = boardRepository.findById(savedId).get();

            // 7. board_file_table에 해당 데이터 save 처리
            BoardFileEntity boardFileEntity = BoardFileEntity.toBoardFileEntity(board, originalFilename, storedFileName);
            boardFileRepository.save(boardFileEntity);

            */

            // 다중 파일 처리
            // 6. board_table에 해당 데이터 save 처리
            BoardEntity boardEntity = BoardEntity.toSaveFileEntity(boardDTO);
            Long savedId = boardRepository.save(boardEntity).getId();
            BoardEntity board = boardRepository.findById(savedId).get();

            for (MultipartFile boardFile: boardDTO.getBoardFile()) { // 1. DTO에 담긴 파일을 꺼냄
                // 2. 파일의 이름 가져옴
                String originalFilename = boardFile.getOriginalFilename();

                // 3. 서버 저장용 이름을 만듦
                // 내사진.jpg -> 839798375892_내사진.jpg
                String storedFileName = System.currentTimeMillis() + "_" + originalFilename;

                // 4. 저장 경로 설정
                String savePath = "C:/Users/hanyo/Desktop/project/[3-2]Clug_Server_Study/" + storedFileName; // C:/springboot_img/9802398403948_내사진.jpg

                // 5. 해당 경로에 파일 저장
                boardFile.transferTo(new File(savePath));

                // 7. board_file_table에 해당 데이터 save 처리
                BoardFileEntity boardFileEntity = BoardFileEntity.toBoardFileEntity(board, originalFilename, storedFileName);
                boardFileRepository.save(boardFileEntity);
            }
        }

        System.out.println("테스트");
    }

    @Transactional
    public List<BoardDTO> findAll() {
        List<BoardEntity> boardEntityList = boardRepository.findAll();
        List<BoardDTO> boardDTOList = new ArrayList<>();

        for (BoardEntity boardEntity : boardEntityList) {
            boardDTOList.add(BoardDTO.toBoardDTO(boardEntity));
        }
        return boardDTOList;
    }

    @Transactional
    public void updateHits(Long id) {
        boardRepository.updateHits(id);
    }

    @Transactional
    public BoardDTO findById(Long id) {
        Optional<BoardEntity> optionalBoardEntity = boardRepository.findById(id);
        if (optionalBoardEntity.isPresent()) {
            BoardEntity boardEntity = optionalBoardEntity.get();
            BoardDTO boardDTO = BoardDTO.toBoardDTO(boardEntity);
            return boardDTO;
        } else {
            return null;
        }
    }

    public BoardDTO update(BoardDTO boardDTO) {
        BoardEntity boardEntity = BoardEntity.toUpdateEntity(boardDTO);
        boardRepository.save(boardEntity);
        return findById(boardDTO.getId());
    }

    public void delete(Long id) {
        boardRepository.deleteById(id);
    }

    public Page<BoardDTO> paging(Pageable pageable) {
        int page = pageable.getPageNumber() - 1;
        int pageLimit = 3; // 한 페이지에 볼 글 개수

        // 한 페이지당 3개씩 글을 보여주고 정렬 기준은 id 기준으로 내림차순 정렬
        // page 위치에 있는 값은 0부터 시작
        Page<BoardEntity> boardEntities =
                boardRepository.findAll(PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));

        System.out.println("boardEntities.getContent() = " + boardEntities.getContent()); // 요청 페이지에 해당하는 글
        System.out.println("boardEntities.getTotalElements() = " + boardEntities.getTotalElements()); // 전체 글 개수
        System.out.println("boardEntities.getNumber() = " + boardEntities.getNumber()); // DB로 요청한 페이지 번호
        System.out.println("boardEntities.getTotalPages() = " + boardEntities.getTotalPages()); // 전체 페이지 개수
        System.out.println("boardEntities.getSize() = " + boardEntities.getSize()); // 한 페이지에 보여지는 글 개수
        System.out.println("boardEntities.hasPrevious() = " + boardEntities.hasPrevious()); // 이전 페이지 존재 여부
        System.out.println("boardEntities.isFirst() = " + boardEntities.isFirst()); // 첫 페이지 여부
        System.out.println("boardEntities.isLast() = " + boardEntities.isLast()); // 마지막 페이지 여부

        // 목록 : id, writer, title, hits, createdTime
        Page<BoardDTO> boardDTOS = boardEntities.map(board -> new BoardDTO(board.getId(), board.getBoardWriter(), board.getBoardTitle(), board.getBoardHits(), board.getCreatedTime()));
        return boardDTOS;
    }
}
