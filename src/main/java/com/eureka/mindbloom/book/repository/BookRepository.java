package com.eureka.mindbloom.book.repository;

import com.eureka.mindbloom.book.domain.Book;
import com.eureka.mindbloom.book.dto.BooksResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, String> {
    Optional<Book> findBookByIsbn(String isbn);

    // 검색어 조회
    Slice<Book> findByTitleContainingOrAuthorContaining(String title, String author, Pageable pageable);

    // 검색어 조회 by 좋아요순
    @Query("SELECT b FROM Book b LEFT JOIN BookLikeCount blc ON b.isbn = blc.book.isbn " +
            "WHERE b.title LIKE %:search% OR b.author LIKE %:search% ")
    Slice<Book> findByTitleContainingOrAuthorContainingSortedByLikes(@Param("search") String search, Pageable pageable);

    @Query("SELECT b.isbn FROM Book b JOIN BookCategory bc ON b.isbn = bc.id.isbn WHERE bc.id.categoryTraitId.categoryCode IN :preferences ORDER BY b.createdAt DESC LIMIT 2")
    List<String> findIsbnByCategoryCodeSortRecent(List<String> preferences);

    @Query("SELECT b.isbn FROM Book b WHERE b.isbn IN :isbns AND b.isbn NOT IN (SELECT distinct bv.book.isbn FROM BookView bv WHERE bv.child.id = :childId)")
    List<String> findNotReadIsbnByIsbnAndBookView(Long childId, List<String> isbns);

    @Query("SELECT b.isbn FROM Book b WHERE b.isbn IN :isbns AND b.isbn NOT IN :notReadRecommendBooks")
    List<String> findIsbnByIsbnAndNotReadRecommendBooks(List<String> isbns, List<String> notReadRecommendBooks);

    @Query("SELECT b.isbn FROM Book b WHERE b.isbn IN :books ORDER BY b.viewCount DESC LIMIT 10")
    List<String> findIsbnByBooksInOrderByViewCount(List<String> books);

    @Query("SELECT b.isbn FROM Book b ORDER BY b.viewCount DESC LIMIT 10")
    List<String> findIsbnByOrderByViewCount();


    // 카테고리 조회
    @Query("SELECT b FROM Book b JOIN BookCategory bc ON b.isbn = bc.id.isbn " +
            "WHERE bc.id.categoryTraitId.categoryCode = :categoryCode")
    Slice<Book> findByCategoryCode(@Param("categoryCode") String categoryCode, Pageable pageable);

    // 카테고리 조회 by 좋아요순
    @Query("SELECT b FROM Book b JOIN BookCategory bc ON b.isbn = bc.id.isbn " +
            "LEFT JOIN BookLikeCount blc ON b.isbn = blc.book.isbn " +
            "WHERE bc.id.categoryTraitId.categoryCode = :categoryCode ")
    Slice<Book> findByCategoryCodeSortedByLikes(@Param("categoryCode") String categoryCode, Pageable pageable);

    // 카테고리, 검색어 조회
    @Query("SELECT b FROM Book b JOIN BookCategory bc ON b.isbn = bc.id.isbn " +
            "WHERE bc.id.categoryTraitId.categoryCode = :categoryCode " +
            "AND (b.title LIKE %:search% OR b.author LIKE %:search%)")
    Slice<Book> findByCategoryCodeAndTitleContainingOrAuthorContaining(
            @Param("categoryCode") String categoryCode,
            @Param("search") String search,
            Pageable pageable
    );

    // 카테고리, 검색어 조회 by 좋아요순
    @Query("SELECT b FROM Book b JOIN BookCategory bc ON b.isbn = bc.id.isbn " +
            "LEFT JOIN BookLikeCount blc ON b.isbn = blc.book.isbn " +
            "WHERE bc.id.categoryTraitId.categoryCode = :categoryCode " +
            "AND (b.title LIKE %:search% OR b.author LIKE %:search%) ")
    Slice<Book> findByCategoryCodeAndTitleContainingOrAuthorContainingSortedByLikes(
            @Param("categoryCode") String categoryCode,
            @Param("search") String search,
            Pageable pageable
    );

    // 모든 책 조회 with 좋아요수
    @Query("SELECT b FROM Book b LEFT JOIN BookLikeCount blc ON b.isbn = blc.book.isbn")
    Slice<Book> findAllBooksSortedByLikes(Pageable pageable);

    @Query(value = """
            SELECT new com.eureka.mindbloom.book.dto.BooksResponse(b.isbn, b.title, b.author, b.coverImage) FROM Book b
            JOIN BookView bv ON b.isbn = bv.book.isbn
            WHERE bv.child.id = :childId
            ORDER BY bv.id DESC
            """)
    Slice<BooksResponse> findRecentlyReadBook(Pageable pageable, Long childId);
}
