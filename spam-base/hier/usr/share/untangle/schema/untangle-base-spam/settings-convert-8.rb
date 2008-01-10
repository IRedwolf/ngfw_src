def update_schema()
  sql_helper = SqlHelper.new(@dbh)

  sql_helper.rename_column('settings.n_spam_settings', 'smtp_inbound',
                           'smtp_config')
  sql_helper.rename_column('settings.n_spam_settings', 'pop_outbound',
                           'pop_config')
  sql_helper.rename_column('settings.n_spam_settings', 'imap_outbound',
                           'imap_config')

  sql_helper.remove_columns('settings.n_spam_settings',
                            [ 'smtp_outbound', 'pop_inbound', 'imap_inbound' ])

  schema_rewrite = [
   'DELETE FROM n_spam_smtp_config
WHERE config_id NOT IN (SELECT smtp_config FROM n_spam_settings)',
   'DELETE FROM n_spam_imap_config
WHERE config_id NOT IN (SELECT imap_config FROM n_spam_settings)',
   'DELETE FROM n_spam_pop_config
WHERE config_id NOT IN (SELECT pop_config FROM n_spam_settings)'
  ]

  memo_update = [ 'n_spam_smtp_config', 'n_spam_imap_config', 'n_spam_pop_config' ].map do |o|
    [ "UPDATE #{o} SET notes = replace(notes, ' incoming', '')",
      "UPDATE #{o} SET notes = replace(notes, ' outgoing', '')" ]
  end.flatten

  (schema_rewrite + memo_update).each do |sql|
    begin
      @dbh.do(sql)
    rescue DBI::DatabaseError => e
      SqlHelper.log_sql_error("Could not run #{sql}", e)
    end
  end

end
